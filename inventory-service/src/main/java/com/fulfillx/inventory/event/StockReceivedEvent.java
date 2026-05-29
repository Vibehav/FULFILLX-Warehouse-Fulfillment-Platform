package com.fulfillx.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockReceivedEvent {

    private String grnId;
    private String warehouseId;
    private String tenantId;
    private List<StockItem> items;
    private LocalDateTime receivedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StockItem {
        private String skuId;
        private Integer receivedQuantity;
    }
}