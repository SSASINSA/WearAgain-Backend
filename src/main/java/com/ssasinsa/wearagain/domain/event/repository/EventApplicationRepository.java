package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {

    boolean existsByUserIdAndEventOptionIdAndStatusIn(Long userId, Long eventOptionId, Collection<EventApplicationStatus> statuses);

    boolean existsByUserIdAndEventIdAndStatusIn(Long userId, Long eventId, Collection<EventApplicationStatus> statuses);

    long countByEventOptionIdAndStatusIn(Long eventOptionId, Collection<EventApplicationStatus> statuses);

    Optional<EventApplication> findByIdAndUserId(Long applicationId, Long userId);

    @Query("""
            select new com.ssasinsa.wearagain.domain.event.repository.EventOptionApplicationCount(
                ea.eventOption.id,
                count(ea)
            )
            from EventApplication ea
            where ea.eventOption.id in :optionIds
            and ea.status in :statuses
            group by ea.eventOption.id
            """)
    List<EventOptionApplicationCount> countActiveApplicationsByOptionIds(
            @Param("optionIds") Collection<Long> optionIds,
            @Param("statuses") Collection<EventApplicationStatus> statuses
    );

    @Query("""
            select new com.ssasinsa.wearagain.domain.event.repository.EventApplicationEventCount(
                ea.event.id,
                count(ea)
            )
            from EventApplication ea
            where ea.event.id in :eventIds
            and ea.status in :statuses
            group by ea.event.id
            """)
    List<EventApplicationEventCount> countActiveApplicationsByEventIds(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("statuses") Collection<EventApplicationStatus> statuses
    );

    @Query("""
            select ea from EventApplication ea
            left join fetch ea.user
            left join fetch ea.eventOption
            where ea.event.id = :eventId
            order by ea.createdAt asc
            """)
    List<EventApplication> findAllWithUserByEventId(@Param("eventId") Long eventId);

    @Query("""
            select ea.id from EventApplication ea
            where ea.user.id = :userId
            and ea.status in :statuses
            and (:from is null or ea.createdAt >= :from)
            and (:to is null or ea.createdAt < :to)
            and (
                :cursorCreatedAt is null
                or ea.createdAt < :cursorCreatedAt
                or (ea.createdAt = :cursorCreatedAt and ea.id < :cursorId)
            )
            order by ea.createdAt desc, ea.id desc
            """)
    List<Long> findApplicationIdsForUser(
            @Param("userId") Long userId,
            @Param("statuses") Collection<EventApplicationStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Query("""
            select distinct ea from EventApplication ea
            join fetch ea.event e
            left join fetch e.images
            left join fetch ea.eventOption eo
            where ea.id in :ids
            """)
    List<EventApplication> findByIdsWithEventAndImages(@Param("ids") Collection<Long> ids);

    @EntityGraph(attributePaths = {
            "eventOption",
            "eventOption.parentOption",
            "eventOption.parentOption.parentOption"
    })
    Optional<EventApplication> findTopByUserIdAndEventIdOrderByCreatedAtDescIdDesc(Long userId, Long eventId);

    @Query("select count(a) from EventApplication a where a.status = com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus.CHECKED_IN")
    long countCheckedIn();

    @Query("select new com.ssasinsa.wearagain.domain.event.repository.EventApplicationMetrics(a.event.id, count(a)) "
            + "from EventApplication a "
            + "where a.status = com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus.CHECKED_IN "
            + "and a.event.id in :eventIds "
            + "group by a.event.id")
    List<EventApplicationMetrics> countCheckedInByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
