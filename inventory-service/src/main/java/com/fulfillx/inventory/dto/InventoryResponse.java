package com.fulfillx.inventory.dto;

import com.fulfillx.inventory.enums.InventoryStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class InventoryResponse {
    private String id;
    private String skuId;
    private String warehouseId;
    private String tenantId;
    private Integer totalQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private InventoryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}