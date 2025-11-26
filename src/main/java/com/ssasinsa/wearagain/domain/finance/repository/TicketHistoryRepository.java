package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.TicketHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    @Query("SELECT th.user.id AS userId, SUM(th.changeAmount) AS totalCharged "
            + "FROM TicketHistory th "
            + "WHERE th.relatedEvent.id = :eventId AND th.changeAmount > 0 "
            + "GROUP BY th.user.id")
    List<TicketChargeSummary> calculateChargedTicketsByEvent(@Param("eventId") Long eventId);

    @Query("SELECT COALESCE(SUM(ABS(th.changeAmount)), 0) FROM TicketHistory th WHERE th.user.id = :userId AND th.changeAmount < 0")
    Long sumChangeAmountByUserId(@Param("userId") Long userId);

    interface TicketChargeSummary {

        Long getUserId();

        Long getTotalCharged();
    }
}
