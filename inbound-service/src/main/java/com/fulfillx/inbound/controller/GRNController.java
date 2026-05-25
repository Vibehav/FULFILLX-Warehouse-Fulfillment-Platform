package com.fulfillx.inbound.controller;

import com.fulfillx.inbound.dto.*;
import com.fulfillx.inbound.service.GRNService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound/grn")
@RequiredArgsConstructor
public class GRNController {

    private final GRNService grnService;

    // Create GRN
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INBOUND_OPERATOR', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<GRNResponse> createGRN(
            @Valid @RequestBody CreateGRNRequest request,
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(grnService.createGRN(request, tenantId));
    }

    // Scan Item
    @PutMapping("/scan")
    @PreAuthorize("hasAnyRole('INBOUND_OPERATOR', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<GRNItemResponse> scanItem(
            @Valid @RequestBody ScanItemRequest request) {
        return ResponseEntity.ok(grnService.scanItem(request));
    }

    // Confirm GRN
    @PutMapping("/{grnId}/confirm")
    @PreAuthorize("hasAnyRole('INBOUND_OPERATOR', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<GRNResponse> confirmGRN(
            @PathVariable String grnId) {
        return ResponseEntity.ok(grnService.confirmGRN(grnId));
    }

    // Get GRN by ID
    @GetMapping("/{grnId}")
    @PreAuthorize("hasAnyRole('INBOUND_OPERATOR', 'WAREHOUSE_MANAGER', 'ADMIN')")
    public ResponseEntity<GRNResponse> getGRN(
            @PathVariable String grnId) {
        return ResponseEntity.ok(grnService.getGRN(grnId));
    }

    // Get GRNs by Tenant
    @GetMapping("/tenant")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'ADMIN', 'SELLER')")
    public ResponseEntity<List<GRNResponse>> getGRNsByTenant(
            HttpServletRequest httpRequest) {
        String tenantId = (String) httpRequest.getAttribute("tenantId");
        return ResponseEntity.ok(grnService.getGRNsByTenant(tenantId));
    }
}