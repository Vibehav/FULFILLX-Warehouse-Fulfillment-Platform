package com.fulfillx.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderItemRequest {

    @NotBlank(message = "SKU ID is required")
    private String skuId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;
}