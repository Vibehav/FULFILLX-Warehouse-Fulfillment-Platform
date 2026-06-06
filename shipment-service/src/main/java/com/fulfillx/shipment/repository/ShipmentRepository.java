package com.fulfillx.shipment.repository;

import com.fulfillx.shipment.entity.Shipment;
import com.fulfillx.shipment.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Optional<Shipment> findByOrderId(String orderId);

    List<Shipment> findByTenantId(String tenantId);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByTenantIdAndStatus(String tenantId, ShipmentStatus status);

    List<Shipment> findByWarehouseId(String warehouseId);

    boolean existsByOrderId(String orderId);
}