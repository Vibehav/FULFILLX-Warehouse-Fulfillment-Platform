package com.fulfillx.catalogue.dto;

import com.fulfillx.catalogue.enums.SkuStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateSkuRequest {

    private String name;
    private String description;
    private String category;
    private String unit;
    private BigDecimal weight;
    private SkuStatus status;
    private BigDecimal price;
    private Boolean active;
}
