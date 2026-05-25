package com.fulfillx.inbound.dto;

import com.fulfillx.inbound.enums.GRNItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ScanItemRequest {

    @NotBlank(message = "GRN ID is required ")
    private String grnId;

    @NotBlank(message = "SKU ID is required ")
    private String skuId;

    @Min(value = 0, message = "Received quantity cannot be negative.")
    private Integer receivedQuantity;

    @NotNull(message = "Status is required ")
    private GRNItemStatus status;

    private String rejectionReason;
}