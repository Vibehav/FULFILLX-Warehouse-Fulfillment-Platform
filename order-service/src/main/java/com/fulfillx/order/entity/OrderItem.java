package com.fulfillx.order.entity;

import com.fulfillx.order.enums.OrderItemStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String skuId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderItemStatus status;

    @PrePersist
    protected void onCreate() {
        status = OrderItemStatus.PENDING;
    }
}