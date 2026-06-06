package com.fulfillx.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StockTransferRequest {

    @NotBlank(message = "SKU ID is required")
    private String skuId;

    @NotBlank(message = "Source warehouse ID is required")
    private String fromWarehouseId;

    @NotBlank(message = "Destination warehouse ID is required")
    private String toWarehouseId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}