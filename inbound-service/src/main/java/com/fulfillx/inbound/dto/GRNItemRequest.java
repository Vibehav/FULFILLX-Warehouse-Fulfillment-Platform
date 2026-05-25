package com.fulfillx.inbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GRNItemRequest {

    @NotBlank(message = "SKU ID is required")
    private String skuId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}