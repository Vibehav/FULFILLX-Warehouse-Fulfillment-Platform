package com.fulfillx.inbound.dto;

import com.fulfillx.inbound.enums.GRNItemStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GRNItemResponse {
    private String id;
    private String skuId;
    private Integer quantity;
    private Integer receivedQuantity;
    private GRNItemStatus status;
    private String rejectionReason;
}