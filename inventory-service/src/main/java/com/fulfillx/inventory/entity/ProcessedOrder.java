package com.fulfillx.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_order_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedOrder {

    @Id
    @Column(updatable = false, nullable = false)
    private String orderId;
}
