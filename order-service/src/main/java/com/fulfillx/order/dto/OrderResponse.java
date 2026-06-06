package com.fulfillx.order.dto;

import com.fulfillx.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String sellerId;
    private String tenantId;
    private String warehouseId;
    private OrderStatus status;
    private String remarks;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime deliveredAt;
}