package com.fulfillx.order.controller;

import com.fulfillx.order.dto.*;
import com.fulfillx.order.enums.OrderStatus;
import com.fulfillx.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Create Order
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {

        String sellerId = (String) httpRequest.getAttribute("userId");
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, sellerId, tenantId));
    }

    // Get Order by ID
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId, HttpServletRequest httpRequest) {

        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(orderService.getOrder(orderId, tenantId));
    }

    // Get Orders by Seller
    @GetMapping("/my-orders")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest httpRequest) {
        String sellerId = (String) httpRequest.getAttribute("userId");
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(orderService.getOrdersBySeller(sellerId, tenantId));
    }

    // Get All Orders by Tenant
    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByTenant(
            HttpServletRequest httpRequest) {String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(orderService.getOrdersByTenant(tenantId));
    }

    // Get Orders by Status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status, HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(orderService.getOrdersByStatus(tenantId, status));
    }

    // Cancel Order
    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId,HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");

        return ResponseEntity.ok(orderService.cancelOrder(orderId, tenantId));
    }
}