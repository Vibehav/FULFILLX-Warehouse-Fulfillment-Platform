package com.fulfillx.order.dto;

import com.fulfillx.order.enums.OrderItemStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponse {
    private String id;
    private String skuId;
    private Integer quantity;
    private String warehouseId;
    private OrderItemStatus status;
}