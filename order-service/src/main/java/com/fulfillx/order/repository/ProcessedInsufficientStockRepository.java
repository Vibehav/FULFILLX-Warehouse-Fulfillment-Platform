package com.fulfillx.order.repository;

import com.fulfillx.order.entity.ProcessedInsufficientStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedInsufficientStockRepository extends JpaRepository<ProcessedInsufficientStock, String> {

}
