package com.fulfillx.catalogue.dto;

import com.fulfillx.catalogue.enums.SkuStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateSkuRequest {

    @NotBlank(message = "Enter Sku code")
    private String skuCode;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank
    private String category;

    @NotBlank
    private String unit;

    @NotNull
    @Positive
    private BigDecimal weight;

    @NotBlank
    private String tenantId;

    @NotNull(message = "price is required")
    @Positive
    private BigDecimal price;

}
