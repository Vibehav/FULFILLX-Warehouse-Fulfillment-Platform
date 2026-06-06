package com.fulfillx.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockReservedEvent {

    private String orderId;
    private String tenantId;
    private String warehouseId;
    private List<ReservedItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReservedItem {
        private String skuId;
        private Integer quantity;
    }
}