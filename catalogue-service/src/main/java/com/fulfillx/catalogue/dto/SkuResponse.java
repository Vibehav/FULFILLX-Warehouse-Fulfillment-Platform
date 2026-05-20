package com.fulfillx.catalogue.dto;

import com.fulfillx.catalogue.enums.SkuStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuResponse {

    private String id;
    private String skuCode;
    private String name;
    private String unit;
    private BigDecimal weight;
    private BigDecimal price;
    private Boolean active;
    private String description;
    private String category;
    private String sellerId;
    private String tenantId;
    private SkuStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}