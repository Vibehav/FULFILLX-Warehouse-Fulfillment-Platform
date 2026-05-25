package com.fulfillx.inbound.entity;

import com.fulfillx.inbound.enums.GRNItemStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grn_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", nullable = false)
    private GRN grn;

    @Column(nullable = false)
    private String skuId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer receivedQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GRNItemStatus status;

    @Column
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        status = GRNItemStatus.PENDING;
        receivedQuantity = 0;
    }
}