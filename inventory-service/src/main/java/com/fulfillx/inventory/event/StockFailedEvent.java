package com.fulfillx.inventory.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockFailedEvent { // Order can't be fulfilled (Insufficient Quantity)
    private String orderId;
    private String tenantId;
    private String reason;
}
