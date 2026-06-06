package com.fulfillx.inventory.service;

import com.fulfillx.inventory.config.RedisConfig;
import com.fulfillx.inventory.dto.*;
import com.fulfillx.inventory.entity.Inventory;
import com.fulfillx.inventory.enums.InventoryStatus;
import com.fulfillx.inventory.event.*;
import com.fulfillx.inventory.exception.*;
import com.fulfillx.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryEventPublisher eventPublisher;

    // ===== Called by RabbitMQ Consumer =====
    @Retryable( // Only retry for temporary database locks/concurrency issues
            retryFor = { ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class },
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2)
    )
    @Transactional
    public void updateStockFromGRN(StockReceivedEvent event) {
        log.info(">>> Updating stock from GRN: {}", event.getGrnId());

        for (StockReceivedEvent.StockItem item : event.getItems()) {
            Optional<Inventory> existing = inventoryRepository.findBySkuIdAndWarehouseIdAndTenantId(
                            item.getSkuId(),
                            event.getWarehouseId(),
                            event.getTenantId()
                    );

            if (existing.isPresent()) {
                // Update existing inventory
                Inventory inventory = existing.get();
                inventory.setTotalQuantity(
                        inventory.getTotalQuantity() + item.getReceivedQuantity()
                );
                inventory.setAvailableQuantity(
                        inventory.getAvailableQuantity() + item.getReceivedQuantity()
                );
                inventoryRepository.save(inventory);

                log.info(">>> Stock updated for SKU: {} +{} units", item.getSkuId(), item.getReceivedQuantity());
            } else {
                // Create new inventory record
                Inventory inventory = Inventory.builder()
                        .skuId(item.getSkuId())
                        .warehouseId(event.getWarehouseId())
                        .tenantId(event.getTenantId())
                        .totalQuantity(item.getReceivedQuantity())
                        .reservedQuantity(0)
                        .availableQuantity(item.getReceivedQuantity())
                        .build();
                inventoryRepository.save(inventory);

                log.info(">>> New inventory created for SKU: {}", item.getSkuId());
            }
        }
    }

    // ===== Called by RabbitMQ consumer ( Order Created Event) =====

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2)
    )
    @Transactional
    public void reserveStockForOrder(OrderCreatedEvent event) {
        log.info(">>> Reserving stock for order: {}", event.getOrderId());

        for (OrderCreatedEvent.OrderItem item : event.getItems()) {
            Inventory inventory = inventoryRepository
                    .findBySkuIdAndWarehouseIdAndTenantIdWithLock(
                            item.getSkuId(),
                            item.getWarehouseId(),
                            event.getTenantId()
                    )
                    .orElseThrow(() -> new InventoryNotFoundException(
                            "Inventory not found for SKU: " + item.getSkuId()
                    ));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for SKU: " + item.getSkuId() +
                                " Available: " + inventory.getAvailableQuantity() +
                                " Requested: " + item.getQuantity()
                );
            }

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + item.getQuantity()
            );
            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - item.getQuantity()
            );
            inventoryRepository.save(inventory);
        }

        // Publish STOCK_RESERVED
        eventPublisher.publishStockReserved(
                StockReservedEvent.builder()
                        .orderId(event.getOrderId())
                        .tenantId(event.getTenantId())
                        .warehouseId(event.getWarehouseId())
                        .items(event.getItems().stream()
                                .map(item -> StockReservedEvent.ReservedItem.builder()
                                        .skuId(item.getSkuId())
                                        .quantity(item.getQuantity())
                                        .build()
                                ).collect(Collectors.toList()))
                        .build()
        );

        log.info(">>> Stock reserved for order: {}", event.getOrderId());
    }

    // ===== Reserve Stock =====

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2)
    )
    @Transactional
    @CacheEvict(value = RedisConfig.INVENTORY_CACHE, key = "#request.skuId + '_' + #request.warehouseId")
    public InventoryResponse reserveStock(
            StockReserveRequest request, String tenantId) {

        Inventory inventory = inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantIdWithLock(
                        request.getSkuId(),
                        request.getWarehouseId(),
                        tenantId
                )
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + request.getSkuId()));

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock. Available: " + inventory.getAvailableQuantity() + " Requested: " + request.getQuantity()
            );
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());

        Inventory saved = inventoryRepository.save(inventory);
        log.info(">>> Stock reserved for SKU: {} quantity: {}", request.getSkuId(), request.getQuantity());

        // Publish stock reserved event
        eventPublisher.publishStockReserved(
                StockReservedEvent.builder()
                        .orderId(request.getOrderId())
                        .tenantId(tenantId)
                        .warehouseId(request.getWarehouseId())
                        .items(List.of(
                                StockReservedEvent.ReservedItem.builder()
                                        .skuId(request.getSkuId())
                                        .quantity(request.getQuantity())
                                        .build()
                        ))
                        .build()
        );

        return mapToResponse(saved);
    }

    // ===== Release Stock =====

    @Transactional
    @CacheEvict(value = RedisConfig.INVENTORY_CACHE, key = "#request.skuId + '_' + #request.warehouseId")
    public InventoryResponse releaseStock(
            StockReleaseRequest request, String tenantId) {

        Inventory inventory = inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        request.getSkuId(),
                        request.getWarehouseId(),
                        tenantId
                )
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for SKU: " + request.getSkuId()
                ));

        int newReserved = inventory.getReservedQuantity() - request.getQuantity();
        if (newReserved < 0) newReserved = 0;

        inventory.setReservedQuantity(newReserved);
        inventory.setAvailableQuantity(
                inventory.getTotalQuantity() - newReserved
        );

        Inventory saved = inventoryRepository.save(inventory);
        log.info(">>> Stock released for SKU: {} quantity: {}", request.getSkuId(), request.getQuantity());

        return mapToResponse(saved);
    }

    // ===== Transfer Stock =====

    @Transactional
    public void transferStock(StockTransferRequest request, String tenantId) {

        Inventory source = inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        request.getSkuId(),
                        request.getFromWarehouseId(),
                        tenantId
                )
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Source inventory not found"
                ));

        if (source.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException(
                    "Insufficient stock in source warehouse"
            );
        }

        // Deduct from source
        source.setTotalQuantity(
                source.getTotalQuantity() - request.getQuantity()
        );
        source.setAvailableQuantity(
                source.getAvailableQuantity() - request.getQuantity()
        );
        inventoryRepository.save(source);

        // Add to destination
        Optional<Inventory> destination = inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        request.getSkuId(),
                        request.getToWarehouseId(),
                        tenantId
                );

        if (destination.isPresent()) {
            Inventory dest = destination.get();
            dest.setTotalQuantity(
                    dest.getTotalQuantity() + request.getQuantity()
            );
            dest.setAvailableQuantity(
                    dest.getAvailableQuantity() + request.getQuantity()
            );
            inventoryRepository.save(dest);
        } else {
            Inventory newInventory = Inventory.builder()
                    .skuId(request.getSkuId())
                    .warehouseId(request.getToWarehouseId())
                    .tenantId(tenantId)
                    .totalQuantity(request.getQuantity())
                    .reservedQuantity(0)
                    .availableQuantity(request.getQuantity())
                    .build();
            inventoryRepository.save(newInventory);
        }

        log.info(">>> Stock transferred SKU: {} from: {} to: {} qty: {}",
                request.getSkuId(),
                request.getFromWarehouseId(),
                request.getToWarehouseId(),
                request.getQuantity());
    }

    // ===== Get Stock =====

    @Cacheable(value = RedisConfig.INVENTORY_CACHE,
            key = "#skuId + '_' + #warehouseId")
    public InventoryResponse getStock(
            String skuId, String warehouseId, String tenantId) {
        return inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        skuId, warehouseId, tenantId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for SKU: " + skuId
                ));
    }



    // ===== Get All by Tenant =====

    public List<InventoryResponse> getInventoryByTenant(String tenantId) {
        return inventoryRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===== Private Helpers =====

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .skuId(inventory.getSkuId())
                .warehouseId(inventory.getWarehouseId())
                .tenantId(inventory.getTenantId())
                .totalQuantity(inventory.getTotalQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .status(inventory.getStatus())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}