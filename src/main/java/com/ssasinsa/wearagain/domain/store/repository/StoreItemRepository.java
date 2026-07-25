package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreItemRepository extends JpaRepository<StoreItem, Long>, JpaSpecificationExecutor<StoreItem> {

    List<StoreItem> findByStatusIn(List<StoreItemStatus> statuses);

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE StoreItem i
            SET i.stock = i.stock - :quantity
            WHERE i.id = :itemId
              AND i.status = :status
            """)
    int decreaseStock(
            @Param("itemId") Long itemId,
            @Param("quantity") int quantity,
            @Param("status") StoreItemStatus status
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE StoreItem i
            SET i.stock = i.stock + :quantity
            WHERE i.id = :itemId
            """)
    int increaseStock(
            @Param("itemId") Long itemId,
            @Param("quantity") int quantity
    );

    @Query("""
            SELECT i FROM StoreItem i
            WHERE i.status = :status
              AND (:category IS NULL OR LOWER(i.category) = LOWER(:category))
              AND (:keyword IS NULL
                   OR LOWER(i.name) LIKE CONCAT('%', LOWER(:keyword), '%')
                   OR LOWER(i.description) LIKE CONCAT('%', LOWER(:keyword), '%'))
              AND (:cursorId IS NULL OR i.id < :cursorId)
            ORDER BY i.id DESC
            """)
    List<StoreItem> findActiveItemsWithCursor(
            @Param("status") StoreItemStatus status,
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("cursorId") Long cursorId,
            org.springframework.data.domain.Pageable pageable
    );
}
