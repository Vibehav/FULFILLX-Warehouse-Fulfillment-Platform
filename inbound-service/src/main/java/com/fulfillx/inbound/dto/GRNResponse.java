package com.fulfillx.inbound.dto;

import com.fulfillx.inbound.enums.GRNStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GRNResponse {
    private String id;
    private String vendorId;
    private String warehouseId;
    private String tenantId;
    private GRNStatus status;
    private String remarks;
    private List<GRNItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
}