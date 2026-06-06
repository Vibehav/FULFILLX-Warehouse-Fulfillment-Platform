package com.fulfillx.inventory.repository;

import com.fulfillx.inventory.entity.ProcessedGrn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedGrnRepository extends JpaRepository<ProcessedGrn, String> {
    // The standard .save() method is all we need to trigger the idempotency check.
}