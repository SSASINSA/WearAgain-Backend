package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long>, JpaSpecificationExecutor<StoreItem> {

    List<StoreItem> findByStatusIn(List<StoreItemStatus> statuses);
}
