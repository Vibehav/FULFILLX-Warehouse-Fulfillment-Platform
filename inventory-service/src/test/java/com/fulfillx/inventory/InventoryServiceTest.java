package com.fulfillx.inventory;

import com.fulfillx.inventory.dto.*;
import com.fulfillx.inventory.entity.Inventory;
import com.fulfillx.inventory.enums.InventoryStatus;
import com.fulfillx.inventory.event.*;
import com.fulfillx.inventory.exception.*;
import com.fulfillx.inventory.repository.InventoryRepository;
import com.fulfillx.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory mockInventory;
    private StockReserveRequest reserveRequest;
    private StockReleaseRequest releaseRequest;
    private StockReceivedEvent stockReceivedEvent;

    @BeforeEach
    void setUp() {
        mockInventory = Inventory.builder()
                .id("inv-001")
                .skuId("sku-001")
                .warehouseId("warehouse-001")
                .tenantId("tenant-001")
                .totalQuantity(100)
                .reservedQuantity(0)
                .availableQuantity(100)
                .status(InventoryStatus.ACTIVE)
                .build();

        reserveRequest = new StockReserveRequest();
        reserveRequest.setSkuId("sku-001");
        reserveRequest.setWarehouseId("warehouse-001");
        reserveRequest.setQuantity(10);
        reserveRequest.setOrderId("order-001");

        releaseRequest = new StockReleaseRequest();
        releaseRequest.setSkuId("sku-001");
        releaseRequest.setWarehouseId("warehouse-001");
        releaseRequest.setQuantity(10);
        releaseRequest.setOrderId("order-001");

        stockReceivedEvent = StockReceivedEvent.builder()
                .grnId("grn-001")
                .warehouseId("warehouse-001")
                .tenantId("tenant-001")
                .items(List.of(
                        StockReceivedEvent.StockItem.builder()
                                .skuId("sku-001")
                                .receivedQuantity(50)
                                .build()
                ))
                .build();
    }

    // ✅ Test 1 — Update Stock from GRN (New Inventory)
    @Test
    void updateStockFromGRN_ShouldCreateInventory_WhenNotExists() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenReturn(mockInventory);

        inventoryService.updateStockFromGRN(stockReceivedEvent);

        verify(inventoryRepository, times(1)).save(any());
    }

    // ✅ Test 2 — Update Stock from GRN (Existing Inventory)
    @Test
    void updateStockFromGRN_ShouldUpdateInventory_WhenExists() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any())).thenReturn(mockInventory);

        inventoryService.updateStockFromGRN(stockReceivedEvent);

        verify(inventoryRepository, times(1)).save(any());
        assertEquals(150, mockInventory.getTotalQuantity());
        assertEquals(150, mockInventory.getAvailableQuantity());
    }

    // ✅ Test 3 — Reserve Stock Successfully
    @Test
    void reserveStock_ShouldReserveSuccessfully_WhenSufficientStock() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantIdWithLock(any(), any(), any()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any())).thenReturn(mockInventory);
        doNothing().when(eventPublisher).publishStockReserved(any());

        InventoryResponse response = inventoryService
                .reserveStock(reserveRequest, "tenant-001");

        assertNotNull(response);
        assertEquals(10, mockInventory.getReservedQuantity());
        assertEquals(90, mockInventory.getAvailableQuantity());
        verify(eventPublisher, times(1)).publishStockReserved(any());
    }

    // ✅ Test 4 — Reserve Stock Fails if Insufficient
    @Test
    void reserveStock_ShouldThrowException_WhenInsufficientStock() {
        mockInventory.setAvailableQuantity(5);
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantIdWithLock(any(), any(), any()))
                .thenReturn(Optional.of(mockInventory));

        reserveRequest.setQuantity(10);

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserveStock(reserveRequest, "tenant-001"));

        verify(eventPublisher, never()).publishStockReserved(any());
    }

    // ✅ Test 5 — Reserve Stock Fails if Not Found
    @Test
    void reserveStock_ShouldThrowException_WhenInventoryNotFound() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantIdWithLock(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.reserveStock(reserveRequest, "tenant-001"));
    }

    // ✅ Test 6 — Release Stock Successfully
    @Test
    void releaseStock_ShouldReleaseSuccessfully_WhenValidRequest() {
        mockInventory.setReservedQuantity(10);
        mockInventory.setAvailableQuantity(90);

        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository.save(any())).thenReturn(mockInventory);

        InventoryResponse response = inventoryService
                .releaseStock(releaseRequest, "tenant-001");

        assertNotNull(response);
        assertEquals(0, mockInventory.getReservedQuantity());
        assertEquals(100, mockInventory.getAvailableQuantity());
    }


    // ✅ Test 7 — Get Stock Successfully
    @Test
    void getStock_ShouldReturnInventory_WhenExists() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.of(mockInventory));

        InventoryResponse response = inventoryService
                .getStock("sku-001", "warehouse-001", "tenant-001");

        assertNotNull(response);
        assertEquals("sku-001", response.getSkuId());
        assertEquals(100, response.getAvailableQuantity());
    }

    // ✅ Test 8 — Get Stock Fails if Not Found
    @Test
    void getStock_ShouldThrowException_WhenNotFound() {
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(any(), any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class,
                () -> inventoryService.getStock(
                        "invalid", "warehouse-001", "tenant-001"));
    }

    // ✅ Test 9 — Transfer Stock Successfully
    @Test
    void transferStock_ShouldTransfer_WhenSufficientStock() {
        StockTransferRequest transferRequest = new StockTransferRequest();
        transferRequest.setSkuId("sku-001");
        transferRequest.setFromWarehouseId("warehouse-001");
        transferRequest.setToWarehouseId("warehouse-002");
        transferRequest.setQuantity(20);

        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        "sku-001", "warehouse-001", "tenant-001"))
                .thenReturn(Optional.of(mockInventory));
        when(inventoryRepository
                .findBySkuIdAndWarehouseIdAndTenantId(
                        "sku-001", "warehouse-002", "tenant-001"))
                .thenReturn(Optional.empty());
        when(inventoryRepository.save(any())).thenReturn(mockInventory);

        inventoryService.transferStock(transferRequest, "tenant-001");

        verify(inventoryRepository, times(2)).save(any());
    }
}