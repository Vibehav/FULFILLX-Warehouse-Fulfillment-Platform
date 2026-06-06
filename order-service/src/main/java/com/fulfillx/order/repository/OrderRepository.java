package com.fulfillx.order.repository;

import com.fulfillx.order.entity.Order;
import com.fulfillx.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByTenantId(String tenantId);

    List<Order> findBySellerId(String sellerId);

    List<Order> findByTenantIdAndStatus(String tenantId, OrderStatus status);

    List<Order> findByWarehouseId(String warehouseId);

    List<Order> findBySellerIdAndTenantId(String sellerId, String tenantId);
}