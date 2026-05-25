package com.fulfillx.inbound.repository;

import com.fulfillx.inbound.entity.GRNItem;
import com.fulfillx.inbound.enums.GRNItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GRNItemRepository extends JpaRepository<GRNItem,String> {

    //Get all items in a grn
    List<GRNItem> findByGrnId(String grnId);

    //get only accepted , rejected items
    List<GRNItem> findByGrnIdAndStatus(String grnId, GRNItemStatus status);

    // track which grn contains a sku
    List<GRNItem> findBySkuId(String skuId);


    Optional<GRNItem> findByGrnIdAndSkuId(String grnId, String skuId);
}
