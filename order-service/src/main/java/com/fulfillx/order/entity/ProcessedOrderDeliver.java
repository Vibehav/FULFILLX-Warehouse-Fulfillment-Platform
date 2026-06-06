package com.fulfillx.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_order_deliver_ledger")
public class ProcessedOrderDeliver {

    @Id
    @Column(nullable = false,updatable = false)
    private String orderId;
}
