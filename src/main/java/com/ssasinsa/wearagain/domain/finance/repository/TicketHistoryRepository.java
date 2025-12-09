package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.TicketHistory;

import java.util.Collection;
import java.util.List;
import java.time.LocalDateTime;
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

    @Query("SELECT COALESCE(SUM(th.changeAmount), 0) "
            + "FROM TicketHistory th "
            + "WHERE th.createdAt >= :startInclusive AND th.createdAt < :endExclusive")
    Long sumChangeAmountBetween(@Param("startInclusive") LocalDateTime startInclusive,
                                @Param("endExclusive") LocalDateTime endExclusive);

    @Query("select COALESCE(SUM(th.changeAmount), 0) from TicketHistory th where th.changeAmount > 0")
    java.util.Optional<Long> sumPositiveAmounts();

    @Query("select COALESCE(SUM(ABS(th.changeAmount)), 0) from TicketHistory th where th.changeAmount < 0")
    java.util.Optional<Long> sumNegativeAmountsAbs();

    @Query("select new com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryEventSum(th.relatedEvent.id, COALESCE(SUM(th.changeAmount), 0)) "
            + "from TicketHistory th "
            + "where th.relatedEvent.id in :eventIds "
            + "and th.changeAmount > 0 "
            + "group by th.relatedEvent.id")
    List<TicketHistoryEventSum> sumPositiveAmountsByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("select new com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryEventSum(th.relatedEvent.id, COALESCE(SUM(ABS(th.changeAmount)), 0)) "
            + "from TicketHistory th "
            + "where th.relatedEvent.id in :eventIds "
            + "and th.changeAmount < 0 "
            + "group by th.relatedEvent.id")
    List<TicketHistoryEventSum> sumNegativeAmountsAbsByEventIds(@Param("eventIds") Collection<Long> eventIds);

    @Query("select th from TicketHistory th where th.relatedEvent.id = :eventId")
    List<TicketHistory> findByRelatedEventId(@Param("eventId") Long eventId);

    interface TicketChargeSummary {

        Long getUserId();

        Long getTotalCharged();
    }
}
