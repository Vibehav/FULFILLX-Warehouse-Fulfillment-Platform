package com.fulfillx.shipment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_order_confirmed_ledger")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrderConfirmed {
    @Id
    private String orderId;
}
