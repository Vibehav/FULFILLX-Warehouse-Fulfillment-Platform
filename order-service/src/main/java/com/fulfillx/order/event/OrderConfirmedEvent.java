package com.fulfillx.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmedEvent {

    private String orderId;
    private String tenantId;
    private String warehouseId;
    private String sellerId;
}