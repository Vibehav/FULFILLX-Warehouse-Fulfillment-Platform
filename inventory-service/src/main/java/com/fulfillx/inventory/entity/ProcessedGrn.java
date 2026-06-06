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
@Table(name = "processed_grn_ledger")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedGrn {

    // The GRN ID is the primary key. This enforces the unique constraint!
    @Id
    @Column(updatable = false, nullable = false)
    private String grnId;



}