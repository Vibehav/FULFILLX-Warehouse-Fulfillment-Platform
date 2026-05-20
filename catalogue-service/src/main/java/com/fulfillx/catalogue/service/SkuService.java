package com.fulfillx.catalogue.service;

import com.fulfillx.catalogue.dto.CreateSkuRequest;
import com.fulfillx.catalogue.dto.SkuResponse;
import com.fulfillx.catalogue.dto.UpdateSkuRequest;
import com.fulfillx.catalogue.entity.Sku;
import com.fulfillx.catalogue.enums.SkuStatus;
import com.fulfillx.catalogue.exception.SkuAlreadyExistsException;
import com.fulfillx.catalogue.exception.SkuNotFoundException;
import com.fulfillx.catalogue.repository.SkuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final SkuRepository skuRepository;

    public SkuResponse createSku(CreateSkuRequest request,String tenantId){

        if(skuRepository.existsBySkuCode(request.getSkuCode())){
            throw new SkuAlreadyExistsException();
        }

        Sku sku = Sku.builder()
                .skuCode(request.getSkuCode())
                .name(request.getName())
                .description(request.getDescription())
                .unit(request.getUnit())
                .tenantId(request.getTenantId())
                .price(request.getPrice())
                .category(request.getCategory())
                .weight(request.getWeight())
                .build();

        sku = skuRepository.save(sku);

        return mapToResponse(sku);
    }

    // get SKUs by SkuID
    public SkuResponse getSkuById(String skuId){
        Sku sku = skuRepository.findById(skuId).orElseThrow(() -> new SkuNotFoundException("Id is not present."));
        return mapToResponse(sku);
    }

    // get SKU by Code
    public SkuResponse getSkuByCode(String skuCode) {
        Sku sku = skuRepository.findBySkuCode(skuCode).orElseThrow(() -> new SkuNotFoundException("SKU not found: " + skuCode));
        return mapToResponse(sku);
    }

    // get SKUs by Seller
    public List<SkuResponse> getSkusByTenant(String tenantId) {
        return skuRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Get Active SKUs by Seller
    public List<SkuResponse> getActiveSkusByTenant(String tenantId) {
        return skuRepository.findByTenantIdAndStatus(tenantId, SkuStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Update SKU
    public SkuResponse updateSku(String skuId, UpdateSkuRequest request) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found: " + skuId));

        if (request.getName() != null) sku.setName(request.getName());
        if (request.getDescription() != null) sku.setDescription(request.getDescription());
        if (request.getCategory() != null) sku.setCategory(request.getCategory());
        if (request.getUnit() != null) sku.setUnit(request.getUnit());
        if (request.getWeight() != null ) sku.setWeight(request.getWeight());
        if (request.getStatus() != null) sku.setStatus(request.getStatus());
        if (request.getActive() != null) sku.setActive(request.getActive());
        if(request.getPrice() != null) sku.setPrice(request.getPrice());


        return mapToResponse(skuRepository.save(sku));
    }

    // Delete SKU
    public void deleteSku(String skuId) {
        Sku sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new SkuNotFoundException("SKU not found: " + skuId));
        skuRepository.delete(sku);
    }

    private SkuResponse mapToResponse(Sku sku) {

        return SkuResponse.builder()
                .id(sku.getId())
                .skuCode(sku.getSkuCode())
                .name(sku.getName())
                .description(sku.getDescription())
                .category(sku.getCategory())
                .unit(sku.getUnit())
                .weight(sku.getWeight())
                .price(sku.getPrice())
                .tenantId(sku.getTenantId())
                .status(sku.getStatus())
                .active(sku.isActive())
                .createdAt(sku.getCreatedAt())
                .updatedAt(sku.getUpdatedAt())
                .build();
    }

}
