package com.fulfillx.inbound.entity;

import com.fulfillx.inbound.enums.GRNStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRN {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String vendorId;

    @Column(nullable = false)
    private String warehouseId;

    @Column(nullable = false)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GRNStatus status;

    @Column
    private String remarks;

    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<GRNItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime confirmedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        status = GRNStatus.DRAFT;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}