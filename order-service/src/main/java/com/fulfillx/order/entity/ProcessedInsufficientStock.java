package com.fulfillx.order.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "processed_insufficient_stock_ledger")
@Entity
public class ProcessedInsufficientStock {
    @Id
    private String orderId;
}
