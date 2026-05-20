package com.fulfillx.catalogue.repository;

import com.fulfillx.catalogue.entity.Sku;
import com.fulfillx.catalogue.enums.SkuStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku,String> {

    Optional<Sku> findBySkuCode(String skuCode);
    boolean existsBySkuCode(String skuCode);


    List<Sku> findByTenantId(String tenantId);

    List<Sku> findByTenantIdAndStatus(String tenantId, SkuStatus status);

    List<Sku> findByCategory(String category);
}
