package com.fulfillx.order.repository;

import com.fulfillx.order.entity.ProcessedReservedStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedReservedStockRepository extends JpaRepository<ProcessedReservedStock, String> {
}
