package com.fulfillx.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockFailedEvent {

    private String tenantId;
    private String orderId;
    private String reason;

}
