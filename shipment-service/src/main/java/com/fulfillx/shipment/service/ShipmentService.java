package com.fulfillx.shipment.service;

import com.fulfillx.shipment.entity.Shipment;
import com.fulfillx.shipment.enums.CourierPartner;
import com.fulfillx.shipment.enums.ShipmentStatus;
import com.fulfillx.shipment.event.OrderConfirmedEvent;
import com.fulfillx.shipment.event.OrderDeliveredEvent;
import com.fulfillx.shipment.event.ShipmentPublisher;
import com.fulfillx.shipment.exception.ShipmentAlreadyExistsException;
import com.fulfillx.shipment.exception.ShipmentNotFoundException;
import com.fulfillx.shipment.exception.InvalidShipmentStateException;
import com.fulfillx.shipment.dto.ShipmentResponse;
import com.fulfillx.shipment.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentPublisher eventPublisher;

    // Create Shipment (called by RabbitMQ consumer)
    @Transactional
    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class, CannotAcquireLockException.class},
            maxAttempts = 4,
            backoff = @Backoff(delay = 150, multiplier = 2))
    public void createShipment(OrderConfirmedEvent event) {

        if (shipmentRepository.existsByOrderId(event.getOrderId())) {
            log.warn(">>> Shipment already exists for order: {}", event.getOrderId());
            throw new ShipmentAlreadyExistsException("Shipment already exists for order: " + event.getOrderId());
        }

        // Mock courier allocation
        CourierPartner courier = helper(); // allocates courier

        // Mock tracking ID
        String trackingId = generateTrackingId(courier); //Generates mock tracking id with prefix

        Shipment shipment = Shipment.builder()
                .orderId(event.getOrderId())
                .tenantId(event.getTenantId())
                .warehouseId(event.getWarehouseId())
                .sellerId(event.getSellerId())
                .courierPartner(courier)
                .trackingId(trackingId)
                .build();

        shipmentRepository.save(shipment);
        log.info(">>> Shipment created for order: {} courier: {} tracking: {}", event.getOrderId(), courier, trackingId);
    }

    // Mark Delivered
    @Transactional
    public ShipmentResponse markDelivered(String shipmentId, String tenantId) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        if (!shipment.getTenantId().equals(tenantId)) {
            throw new InvalidShipmentStateException("Not authorized to update this shipment");
        }

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new InvalidShipmentStateException("Shipment already delivered");
        }

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(LocalDateTime.now());
        shipmentRepository.save(shipment);

        // Publish ORDER DELIVERED event
        eventPublisher.publishOrderDelivered(
                OrderDeliveredEvent.builder()
                        .orderId(shipment.getOrderId())
                        .tenantId(shipment.getTenantId())
                        .build()
        );

        log.info(">>> Shipment delivered: {}", shipmentId);
        return mapToResponse(shipment);
    }

    // Get Shipment by ID
    public ShipmentResponse getShipment(String shipmentId, String tenantId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        if (!shipment.getTenantId().equals(tenantId)) {
            throw new InvalidShipmentStateException("Not authorized to view this shipment");
        }

        return mapToResponse(shipment);
    }

    // Get Shipment by Order ID
    public ShipmentResponse getShipmentByOrder(String orderId, String tenantId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found for order: " + orderId));

        if (!shipment.getTenantId().equals(tenantId)) {
            throw new InvalidShipmentStateException("Not authorized to view this shipment");
        }

        return mapToResponse(shipment);
    }

    // Get All Shipments by Tenant
    public List<ShipmentResponse> getShipmentsByTenant(String tenantId) {
        return shipmentRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get Shipments by Status
    public List<ShipmentResponse> getShipmentsByStatus(String tenantId, ShipmentStatus status) {

        return shipmentRepository
                .findByTenantIdAndStatus(tenantId, status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Mock Courier Allocation
    private CourierPartner helper() {
        CourierPartner[] partners = CourierPartner.values();
        int n = partners.length;
        int index = (int) (Math.random() * n); // randomly assign partners
        return partners[index];
    }

    // Mock Tracking ID Generation
    private String generateTrackingId(CourierPartner courier) {
        String prefix = switch (courier) {
            case DELHIVERY -> "DEL";
            case SHIPROCKET -> "SHI";
            case BLUEDART -> "BLU";
        };

        prefix = prefix + "-" + UUID.randomUUID()
                .toString()
                .substring(0, 8).toUpperCase();

        return prefix;
    }

    // Map to Response
    private ShipmentResponse mapToResponse(Shipment shipment) {
        return ShipmentResponse.builder()
                .id(shipment.getId())
                .orderId(shipment.getOrderId())
                .tenantId(shipment.getTenantId())
                .warehouseId(shipment.getWarehouseId())
                .sellerId(shipment.getSellerId())
                .status(shipment.getStatus())
                .courierPartner(shipment.getCourierPartner())
                .trackingId(shipment.getTrackingId())
                .deliveredAt(shipment.getDeliveredAt())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}