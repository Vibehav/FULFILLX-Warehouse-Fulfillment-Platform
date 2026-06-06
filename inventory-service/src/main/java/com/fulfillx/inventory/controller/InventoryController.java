package com.fulfillx.inventory.controller;

import com.fulfillx.inventory.dto.*;
import com.fulfillx.inventory.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // Get stock by SKU + Warehouse
    @GetMapping("/sku/{skuId}/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SELLER', 'ADMIN', 'INVENTORY_AUDITOR')")
    public ResponseEntity<InventoryResponse> getStock(
            @PathVariable String skuId,
            @PathVariable String warehouseId,
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(
                inventoryService.getStock(skuId, warehouseId, tenantId));
    }

    // Get all inventory by tenant
    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'SELLER', 'ADMIN', 'INVENTORY_AUDITOR')")
    public ResponseEntity<List<InventoryResponse>> getInventoryByTenant(
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(
                inventoryService.getInventoryByTenant(tenantId));
    }

    // Reserve stock
    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryResponse> reserveStock(
            @Valid @RequestBody StockReserveRequest request,
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(
                inventoryService.reserveStock(request, tenantId));
    }

    // Release stock
    @PostMapping("/release")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<InventoryResponse> releaseStock(
            @Valid @RequestBody StockReleaseRequest request,
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(
                inventoryService.releaseStock(request, tenantId));
    }

    // Transfer stock between warehouses
    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<String> transferStock(
            @Valid @RequestBody StockTransferRequest request,
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        inventoryService.transferStock(request, tenantId);
        return ResponseEntity.ok("Stock transferred successfully");
    }
}