package com.fulfillx.catalogue.controller;

import com.fulfillx.catalogue.dto.*;
import com.fulfillx.catalogue.service.SkuService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogue")
@RequiredArgsConstructor
public class SkuController {

    private final SkuService skuService;

    @PreAuthorize("hasAnyRole('CATALOGUE_MANAGER','SELLER')")
    @PostMapping("/sku")
    public ResponseEntity<SkuResponse> createSku(@Valid @RequestBody CreateSkuRequest request, HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        // Extracted token id from token,
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(skuService.createSku(request, tenantId));
    }

    // Anyone authenticated can look up SKU by ID
    @PreAuthorize("hasAnyRole('CATALOGUE_MANAGER', 'SELLER', 'ADMIN', 'WAREHOUSE_MANAGER', 'INBOUND_OPERATOR')")
    @GetMapping("/sku/{skuId}")
    public ResponseEntity<SkuResponse> getSkuById(@PathVariable String skuId) {
        return ResponseEntity.ok(skuService.getSkuById(skuId));
    }

    // Anyone authenticated can look up SKU by code
    @GetMapping("/sku/code/{skuCode}")
    @PreAuthorize("hasAnyRole('CATALOGUE_MANAGER', 'SELLER', 'ADMIN', 'WAREHOUSE_MANAGER', 'INBOUND_OPERATOR')")
    public ResponseEntity<SkuResponse> getSkuByCode(@PathVariable String skuCode) {
        return ResponseEntity.ok(skuService.getSkuByCode(skuCode));
    }

    // Seller views their own SKUs, Admin views any
    @GetMapping("/sku/tenant/{tenantId}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'CATALOGUE_MANAGER')")
    public ResponseEntity<List<SkuResponse>> getSkusByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(skuService.getSkusByTenant(tenantId));
    }

    // Active SKUs — needed by inbound and warehouse too
    @GetMapping("/sku/seller/{tenantId}/active")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN', 'CATALOGUE_MANAGER', 'WAREHOUSE_MANAGER', 'INBOUND_OPERATOR')")
    public ResponseEntity<List<SkuResponse>> getActiveSkusBySeller(@PathVariable String tenantId) {
        return ResponseEntity.ok(skuService.getActiveSkusByTenant(tenantId));
    }

    // Only CATALOGUE_MANAGER or SELLER updates their own SKU
    @PutMapping("/sku/{skuId}")
    @PreAuthorize("hasAnyRole('CATALOGUE_MANAGER', 'SELLER')")
    public ResponseEntity<SkuResponse> updateSku(@PathVariable String skuId, @RequestBody UpdateSkuRequest request) {
        return ResponseEntity.ok(skuService.updateSku(skuId, request));
    }

    // Only ADMIN can delete
    @DeleteMapping("/sku/{skuId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteSku(@PathVariable String skuId) {
        skuService.deleteSku(skuId);
        return ResponseEntity.ok("SKU deleted successfully");
    }
}