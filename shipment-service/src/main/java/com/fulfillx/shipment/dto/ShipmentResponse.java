package com.fulfillx.shipment.dto;

import com.fulfillx.shipment.enums.CourierPartner;
import com.fulfillx.shipment.enums.ShipmentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ShipmentResponse {
    private String id;
    private String orderId;
    private String tenantId;
    private String warehouseId;
    private String sellerId;
    private ShipmentStatus status;
    private CourierPartner courierPartner;
    private String trackingId;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}