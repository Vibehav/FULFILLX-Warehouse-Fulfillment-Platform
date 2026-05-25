package com.fulfillx.inbound.repository;

import com.fulfillx.inbound.entity.GRN;
import com.fulfillx.inbound.enums.GRNStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GRNRepository extends JpaRepository<GRN, String> {

    List<GRN> findByTenantId(String tenantId);
    List<GRN> findByWarehouseId(String warehouseId);
    List<GRN> findByVendorId(String vendorId);

    List<GRN> findByStatus(GRNStatus status);
    List<GRN> findByTenantIdAndStatus(String tenantId, GRNStatus status);
}
