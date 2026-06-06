package com.fulfillx.shipment;

import com.fulfillx.shipment.dto.ShipmentResponse;
import com.fulfillx.shipment.entity.Shipment;
import com.fulfillx.shipment.enums.CourierPartner;
import com.fulfillx.shipment.enums.ShipmentStatus;
import com.fulfillx.shipment.event.OrderConfirmedEvent;
import com.fulfillx.shipment.event.OrderDeliveredEvent;
import com.fulfillx.shipment.event.ShipmentPublisher;
import com.fulfillx.shipment.exception.InvalidShipmentStateException;
import com.fulfillx.shipment.exception.ShipmentAlreadyExistsException;
import com.fulfillx.shipment.exception.ShipmentNotFoundException;
import com.fulfillx.shipment.repository.ShipmentRepository;
import com.fulfillx.shipment.service.ShipmentService;
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
public class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private ShipmentPublisher eventPublisher;

    @InjectMocks
    private ShipmentService shipmentService;

    private Shipment mockShipment;
    private OrderConfirmedEvent orderConfirmedEvent;

    @BeforeEach
    void setUp() {
        mockShipment = Shipment.builder()
                .id("shipment-001")
                .orderId("order-001")
                .tenantId("tenant-001")
                .warehouseId("warehouse-001")
                .sellerId("seller-001")
                .status(ShipmentStatus.CREATED)
                .courierPartner(CourierPartner.DELHIVERY)
                .trackingId("DLV-A1B2C3D4")
                .build();

        orderConfirmedEvent = OrderConfirmedEvent.builder()
                .orderId("order-001")
                .tenantId("tenant-001")
                .warehouseId("warehouse-001")
                .sellerId("seller-001")
                .build();
    }

    // ✅ Test 1 — Create Shipment Successfully
    @Test
    void createShipment_ShouldCreateShipment_WhenOrderConfirmed() {
        when(shipmentRepository.existsByOrderId("order-001"))
                .thenReturn(false);
        when(shipmentRepository.save(any())).thenReturn(mockShipment);

        shipmentService.createShipment(orderConfirmedEvent);

        verify(shipmentRepository, times(1)).save(any());
    }

    // ✅ Test 2 — Create Shipment Fails if Already Exists
    @Test
    void createShipment_ShouldThrowException_WhenShipmentAlreadyExists() {
        when(shipmentRepository.existsByOrderId("order-001"))
                .thenReturn(true);

        assertThrows(ShipmentAlreadyExistsException.class,
                () -> shipmentService.createShipment(orderConfirmedEvent));

        verify(shipmentRepository, never()).save(any());
    }

    // ✅ Test 3 — Mark Delivered Successfully
    @Test
    void markDelivered_ShouldMarkDelivered_WhenValidShipment() {
        when(shipmentRepository.findById("shipment-001"))
                .thenReturn(Optional.of(mockShipment));
        when(shipmentRepository.save(any())).thenReturn(mockShipment);
        doNothing().when(eventPublisher).publishOrderDelivered(any());

        ShipmentResponse response = shipmentService
                .markDelivered("shipment-001", "tenant-001");

        assertNotNull(response);
        assertEquals(ShipmentStatus.DELIVERED, mockShipment.getStatus());
        assertNotNull(mockShipment.getDeliveredAt());
        verify(eventPublisher, times(1)).publishOrderDelivered(any());
    }

    // ✅ Test 4 — Mark Delivered Fails if Already Delivered
    @Test
    void markDelivered_ShouldThrowException_WhenAlreadyDelivered() {
        mockShipment.setStatus(ShipmentStatus.DELIVERED);
        when(shipmentRepository.findById("shipment-001"))
                .thenReturn(Optional.of(mockShipment));

        assertThrows(InvalidShipmentStateException.class,
                () -> shipmentService.markDelivered(
                        "shipment-001", "tenant-001"));

        verify(eventPublisher, never()).publishOrderDelivered(any());
    }

    // ✅ Test 5 — Mark Delivered Fails if Wrong Tenant
    @Test
    void markDelivered_ShouldThrowException_WhenWrongTenant() {
        when(shipmentRepository.findById("shipment-001"))
                .thenReturn(Optional.of(mockShipment));

        assertThrows(InvalidShipmentStateException.class,
                () -> shipmentService.markDelivered(
                        "shipment-001", "wrong-tenant"));
    }

    // ✅ Test 6 — Get Shipment Successfully
    @Test
    void getShipment_ShouldReturnShipment_WhenExists() {
        when(shipmentRepository.findById("shipment-001"))
                .thenReturn(Optional.of(mockShipment));

        ShipmentResponse response = shipmentService
                .getShipment("shipment-001", "tenant-001");

        assertNotNull(response);
        assertEquals("order-001", response.getOrderId());
        assertEquals(CourierPartner.DELHIVERY, response.getCourierPartner());
    }

    // ✅ Test 7 — Get Shipment Fails if Not Found
    @Test
    void getShipment_ShouldThrowException_WhenNotFound() {
        when(shipmentRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(ShipmentNotFoundException.class,
                () -> shipmentService.getShipment(
                        "invalid", "tenant-001"));
    }

    // ✅ Test 8 — Get Shipment by Order ID
    @Test
    void getShipmentByOrder_ShouldReturnShipment_WhenExists() {
        when(shipmentRepository.findByOrderId("order-001"))
                .thenReturn(Optional.of(mockShipment));

        ShipmentResponse response = shipmentService
                .getShipmentByOrder("order-001", "tenant-001");

        assertNotNull(response);
        assertEquals("shipment-001", response.getId());
    }

    // ✅ Test 9 — Get Shipments by Tenant
    @Test
    void getShipmentsByTenant_ShouldReturnList_WhenExists() {
        when(shipmentRepository.findByTenantId("tenant-001"))
                .thenReturn(List.of(mockShipment));

        List<ShipmentResponse> response = shipmentService
                .getShipmentsByTenant("tenant-001");

        assertNotNull(response);
        assertEquals(1, response.size());
    }

    // ✅ Test 10 — Get Shipments by Status
    @Test
    void getShipmentsByStatus_ShouldReturnList_WhenExists() {
        when(shipmentRepository.findByTenantIdAndStatus(
                "tenant-001", ShipmentStatus.CREATED))
                .thenReturn(List.of(mockShipment));

        List<ShipmentResponse> response = shipmentService
                .getShipmentsByStatus("tenant-001", ShipmentStatus.CREATED);

        assertNotNull(response);
        assertEquals(1, response.size());
    }
}