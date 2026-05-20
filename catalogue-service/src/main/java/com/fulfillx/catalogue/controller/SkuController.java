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

    @PreAuthorize("hasRole('CATALOGUE_MANAGER')")
    @PostMapping("/sku")
    public ResponseEntity<SkuResponse> createSku(@Valid @RequestBody CreateSkuRequest request, HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        // Extracted token id from token,
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(skuService.createSku(request, tenantId));
    }

    @GetMapping("/sku/{skuId}")
    public ResponseEntity<SkuResponse> getSkuById(@PathVariable String skuId) {
        return ResponseEntity.ok(skuService.getSkuById(skuId));
    }

    @GetMapping("/sku/code/{skuCode}")
    public ResponseEntity<SkuResponse> getSkuByCode(@PathVariable String skuCode) {
        return ResponseEntity.ok(skuService.getSkuByCode(skuCode));
    }

    @GetMapping("/sku/tenant/{tenantId}")
    public ResponseEntity<List<SkuResponse>> getSkusByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(skuService.getSkusByTenant(tenantId));
    }

    @GetMapping("/sku/seller/{tenantId}/active")
    public ResponseEntity<List<SkuResponse>> getActiveSkusBySeller(@PathVariable String tenantId) {
        return ResponseEntity.ok(skuService.getActiveSkusByTenant(tenantId));
    }

    @PutMapping("/sku/{skuId}")
    public ResponseEntity<SkuResponse> updateSku(@PathVariable String skuId, @RequestBody UpdateSkuRequest request) {
        return ResponseEntity.ok(skuService.updateSku(skuId, request));
    }

    @DeleteMapping("/sku/{skuId}")
    public ResponseEntity<String> deleteSku(@PathVariable String skuId) {
        skuService.deleteSku(skuId);
        return ResponseEntity.ok("SKU deleted successfully");
    }
}