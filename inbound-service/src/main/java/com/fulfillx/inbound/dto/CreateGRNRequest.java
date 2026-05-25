package com.fulfillx.inbound.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateGRNRequest {

    @NotBlank(message = "Vendor ID is required")
    private String vendorId;

    @NotBlank(message = "Warehouse ID is required")
    private String warehouseId;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<GRNItemRequest> items;

    private String remarks;
}