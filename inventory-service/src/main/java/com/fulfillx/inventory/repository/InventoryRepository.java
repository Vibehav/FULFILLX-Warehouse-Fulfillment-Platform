package com.fulfillx.inventory.repository;

import com.fulfillx.inventory.entity.Inventory;
import com.fulfillx.inventory.enums.InventoryStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {

    // Find by SKU + Warehouse + Tenant
    Optional<Inventory> findBySkuIdAndWarehouseIdAndTenantId(
            String skuId, String warehouseId, String tenantId);

    // Find with Optimistic Lock for reservation
    @Lock(LockModeType.OPTIMISTIC) // Tells JPA - when updating a record,check @version record. if @version changed since last read, throw OptimisticLockException
    @Query("SELECT i FROM Inventory i WHERE i.skuId = :skuId " + "AND i.warehouseId = :warehouseId AND i.tenantId = :tenantId")
    Optional<Inventory> findBySkuIdAndWarehouseIdAndTenantIdWithLock(
            @Param("skuId") String skuId,
            @Param("warehouseId") String warehouseId,
            @Param("tenantId") String tenantId);

    // Find all by tenant
    List<Inventory> findByTenantId(String tenantId);

    // Find all by warehouse
    List<Inventory> findByWarehouseId(String warehouseId);

    // Find all by SKU
    List<Inventory> findBySkuId(String skuId);

    // Find low stock items
    List<Inventory> findByTenantIdAndStatus(
            String tenantId, InventoryStatus status);

    // Find all low stock across all tenants
    List<Inventory> findByStatus(InventoryStatus status);
}