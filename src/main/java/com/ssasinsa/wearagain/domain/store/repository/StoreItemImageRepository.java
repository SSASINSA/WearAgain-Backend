package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemImage;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StoreItemImageRepository extends JpaRepository<StoreItemImage, Long> {

    void deleteByStoreItem(StoreItem storeItem);

    @Query("""
            SELECT i FROM StoreItemImage i
            WHERE i.storeItem.id IN :itemIds
              AND i.sortOrder = (
                SELECT MIN(i2.sortOrder) FROM StoreItemImage i2 WHERE i2.storeItem = i.storeItem
              )
            """)
    List<StoreItemImage> findThumbnailsByStoreItemIds(@Param("itemIds") List<Long> itemIds);
}
