package com.fulfillx.inbound.service;

import com.fulfillx.inbound.dto.*;
import com.fulfillx.inbound.entity.GRN;
import com.fulfillx.inbound.entity.GRNItem;
import com.fulfillx.inbound.enums.GRNItemStatus;
import com.fulfillx.inbound.enums.GRNStatus;
import com.fulfillx.inbound.event.InboundEventPublisher;
import com.fulfillx.inbound.event.StockReceivedEvent;
import com.fulfillx.inbound.exception.GRNItemNotFoundException;
import com.fulfillx.inbound.exception.GRNNotFoundException;
import com.fulfillx.inbound.exception.InvalidGRNStateException;
import com.fulfillx.inbound.repository.GRNItemRepository;
import com.fulfillx.inbound.repository.GRNRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class GRNService {
    
    private final GRNRepository grnRepository;
    private final GRNItemRepository grnItemRepository;
    private final InboundEventPublisher inboundEventPublisher;
    
    
    // CREATE GRN
    @Transactional
    public GRNResponse createGRN(CreateGRNRequest request, String tenantId){
        GRN grn = GRN.builder()
                .vendorId(request.getVendorId())
                .warehouseId(request.getWarehouseId())
                .tenantId(tenantId)
                .remarks(request.getRemarks()).build();
        
        GRN saveGrn = grnRepository.save(grn);
        
        List<GRNItem> items = request.getItems().stream()
                .map(itemReq -> GRNItem.builder()
                        .grn(saveGrn)
                        .skuId(itemReq.getSkuId())
                        .quantity(itemReq.getQuantity())
                        .build())
                .toList();
        
        grnItemRepository.saveAll(items);
        saveGrn.setItems(items);
        
        log.info("GRN create : {}",saveGrn.getId());
        return mapToResponse(saveGrn);
    }

    @Transactional
    public GRNItemResponse scanItem(ScanItemRequest request) {

        GRNItem item = grnItemRepository.findByGrnIdAndSkuId(request.getGrnId(),request.getSkuId())
                .orElseThrow(() -> new GRNItemNotFoundException("GRN Item not found"));

        if (item.getGrn().getStatus() != GRNStatus.DRAFT) {
            throw new InvalidGRNStateException("GRN is already confirmed or cancelled");
        }

        item.setReceivedQuantity(request.getReceivedQuantity());
        item.setStatus(request.getStatus());
        item.setRejectionReason(request.getRejectionReason());

        GRNItem savedItem = grnItemRepository.save(item);
        log.info(">>> Item scanned: {} Status: {}", item.getSkuId(), item.getStatus());

        return mapItemToResponse(savedItem);
    }

    // Confirm GRN
    @Transactional
    public GRNResponse confirmGRN(String grnId) {

        GRN grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new GRNNotFoundException("GRN not found"));

        if (grn.getStatus() != GRNStatus.DRAFT) {
            throw new InvalidGRNStateException("Only DRAFT GRNs can be confirmed");
        }

        grn.setStatus(GRNStatus.CONFIRMED);
        grn.setConfirmedAt(LocalDateTime.now());
        grnRepository.save(grn);

        // Get only ACCEPTED items
        List<GRNItem> acceptedItems = grnItemRepository
                .findByGrnIdAndStatus(grnId, GRNItemStatus.ACCEPTED);

        if (!acceptedItems.isEmpty()) {
            // Build and publish event ( Acceptted items only)
            StockReceivedEvent event = StockReceivedEvent.builder()
                    .grnId(grn.getId())
                    .warehouseId(grn.getWarehouseId())
                    .tenantId(grn.getTenantId())
                    .receivedAt(LocalDateTime.now())
                    .items(acceptedItems.stream()
                            .map(item -> StockReceivedEvent.StockItem.builder()
                                    .skuId(item.getSkuId())
                                    .receivedQuantity(item.getReceivedQuantity()) // actual count of quantity
                                    .build()
                            ).toList())
                    .build();

            inboundEventPublisher.publishStockReceived(event);
        }

        log.info(">>> GRN confirmed: {}", grnId);
        return mapToResponse(grn);
    }

    // Get GRN by ID
    public GRNResponse getGRN(String grnId) {
        GRN grn = grnRepository.findById(grnId)
                .orElseThrow(() -> new GRNNotFoundException("GRN not found"));
        return mapToResponse(grn);
    }

    // Get GRNs by Tenant
    public List<GRNResponse> getGRNsByTenant(String tenantId) {
        return grnRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Map GRN to Response
    private GRNResponse mapToResponse(GRN grn) {
        List<GRNItemResponse> itemResponses = grn.getItems() == null ?
                List.of() :
                grn.getItems().stream()
                        .map(this::mapItemToResponse)
                        .toList();

        return GRNResponse.builder()
                .id(grn.getId())
                .vendorId(grn.getVendorId())
                .warehouseId(grn.getWarehouseId())
                .tenantId(grn.getTenantId())
                .status(grn.getStatus())
                .remarks(grn.getRemarks())
                .items(itemResponses)
                .createdAt(grn.getCreatedAt())
                .updatedAt(grn.getUpdatedAt())
                .confirmedAt(grn.getConfirmedAt())
                .build();
    }

    // Map GRNItem to Response
    private GRNItemResponse mapItemToResponse(GRNItem item) {
        return GRNItemResponse.builder()
                .id(item.getId())
                .skuId(item.getSkuId())
                .quantity(item.getQuantity())
                .receivedQuantity(item.getReceivedQuantity())
                .status(item.getStatus())
                .rejectionReason(item.getRejectionReason())
                .build();
    }
}
