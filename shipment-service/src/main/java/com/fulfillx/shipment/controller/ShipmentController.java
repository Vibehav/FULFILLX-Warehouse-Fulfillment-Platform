package com.fulfillx.shipment.controller;

import com.fulfillx.shipment.dto.ShipmentResponse;
import com.fulfillx.shipment.enums.ShipmentStatus;
import com.fulfillx.shipment.service.ShipmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipment")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // Get Shipment by ID
    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String shipmentId, HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(
                shipmentService.getShipment(shipmentId, tenantId));
    }

    // Get Shipment by Order ID
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<ShipmentResponse> getShipmentByOrder(@PathVariable String orderId,
                                                               HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(
                shipmentService.getShipmentByOrder(orderId, tenantId));
    }

    // Get All Shipments by Tenant
    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByTenant( HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(
                shipmentService.getShipmentsByTenant(tenantId));
    }

    // Get Shipments by Status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<ShipmentResponse>> getShipmentsByStatus(@PathVariable ShipmentStatus status,
                                                                       HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(
                shipmentService.getShipmentsByStatus(tenantId, status));
    }

    // Mark Delivered
    @PutMapping("/{shipmentId}/deliver")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<ShipmentResponse> markDelivered( @PathVariable String shipmentId,
                                                           HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(
                shipmentService.markDelivered(shipmentId, tenantId));
    }
}