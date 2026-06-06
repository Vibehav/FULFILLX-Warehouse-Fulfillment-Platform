package com.fulfillx.order.repository;

import com.fulfillx.order.entity.OrderItem;
import com.fulfillx.order.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderId(String orderId);

    List<OrderItem> findByOrderIdAndStatus(String orderId, OrderItemStatus status);

    List<OrderItem> findBySkuId(String skuId);
}