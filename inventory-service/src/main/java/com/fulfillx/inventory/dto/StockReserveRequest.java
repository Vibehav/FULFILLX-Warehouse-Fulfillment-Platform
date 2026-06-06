package com.fulfillx.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StockReserveRequest {

    @NotBlank(message = "SKU ID is required")
    private String skuId;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Order ID is required")
    private String orderId;
}