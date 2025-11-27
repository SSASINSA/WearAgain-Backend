package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long>, JpaSpecificationExecutor<StoreOrder> {

    long countByUserAndItemAndStatus(User user, StoreItem item, StoreOrderStatus status);

    @Query("""
            SELECT o FROM StoreOrder o
            WHERE o.user = :user
              AND (:status IS NULL OR o.status = :status)
              AND (:orderId IS NULL OR o.id < :orderId)
            ORDER BY o.id DESC
            """)
    List<StoreOrder> findAllWithCursor(
            @Param("user") User user,
            @Param("status") StoreOrderStatus status,
            @Param("orderId") Long orderId,
            Pageable pageable
    );
}
