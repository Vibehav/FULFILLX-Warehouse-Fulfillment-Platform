package com.fulfillx.order.repository;

import com.fulfillx.order.entity.ProcessedOrderDeliver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedOrderDeliveryRepository extends JpaRepository<ProcessedOrderDeliver, String> {
    // default save used
}
