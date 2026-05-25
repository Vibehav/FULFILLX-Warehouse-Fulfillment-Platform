package com.fulfillx.inbound.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    public static class StockItem { // Only Accepted Items gets forwarded to the inventory
        private String skuId;
        private Integer receivedQuantity;
    }

}
/** Example
 * GRN confirmed with:
 * SKU001 → ACCEPTED → receivedQuantity: 80
 * SKU002 → ACCEPTED → receivedQuantity: 30
 * SKU003 → REJECTED → skip
 *
 * StockReceivedEvent contains:
 * items: [
 *   { skuId: SKU001, receivedQuantity: 80 },
 *   { skuId: SKU002, receivedQuantity: 30 }
 * ]
 */
