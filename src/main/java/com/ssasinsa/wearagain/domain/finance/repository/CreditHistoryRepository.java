package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {

    @Query("SELECT COALESCE(SUM(ch.changeAmount), 0) "
            + "FROM CreditHistory ch "
            + "WHERE ch.createdAt >= :startInclusive AND ch.createdAt < :endExclusive")
    Long sumChangeAmountBetween(@Param("startInclusive") LocalDateTime startInclusive,
                                @Param("endExclusive") LocalDateTime endExclusive);
}
