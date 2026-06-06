package com.fulfillx.shipment.repository;

import com.fulfillx.shipment.entity.ProcessedOrderConfirmed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedOrderConfirmedRepository extends JpaRepository<ProcessedOrderConfirmed, String> {
}
