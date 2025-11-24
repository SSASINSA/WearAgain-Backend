package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreItemImage;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreItemImageRepository extends JpaRepository<StoreItemImage, Long> {

    void deleteByStoreItem(StoreItem storeItem);
}
