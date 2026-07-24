package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventApplicationRepository extends
        JpaRepository<EventApplication, Long>,
        JpaSpecificationExecutor<EventApplication> {

    boolean existsByUserIdAndEventOptionIdAndStatusIn(Long userId, Long eventOptionId, Collection<EventApplicationStatus> statuses);

    boolean existsByUserIdAndEventIdAndStatusIn(Long userId, Long eventId, Collection<EventApplicationStatus> statuses);

    long countByEventOptionIdAndStatusIn(Long eventOptionId, Collection<EventApplicationStatus> statuses);

    Optional<EventApplication> findByIdAndUserId(Long applicationId, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select ea from EventApplication ea
            where ea.id = :applicationId
            and ea.user.id = :userId
            """)
    Optional<EventApplication> findByIdAndUserIdForUpdate(
            @Param("applicationId") Long applicationId,
            @Param("userId") Long userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ea from EventApplication ea where ea.id = :applicationId")
    Optional<EventApplication> findByIdForUpdate(@Param("applicationId") Long applicationId);

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
            select distinct ea from EventApplication ea
            left join fetch ea.event e
            left join fetch e.organizerAdmin
            left join fetch ea.user
            left join fetch ea.eventOption eo
            left join fetch eo.parentOption pop
            left join fetch pop.parentOption
            where ea.id in :ids and e.id = :eventId
            """)
    List<EventApplication> findAllWithAssociationsByEventIdAndIdIn(
            @Param("eventId") Long eventId,
            @Param("ids") Collection<Long> ids
    );

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
            "event",
            "event.organizerAdmin",
            "eventOption",
            "eventOption.parentOption",
            "eventOption.parentOption.parentOption",
            "user"
    })
    Page<EventApplication> findAll(Specification<EventApplication> specification, Pageable pageable);

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

    long countByEvent_IdAndStatusIn(Long eventId, Collection<EventApplicationStatus> statuses);

    long countByEventOption_IdIn(Collection<Long> optionIds);

    @Query("""
            select ea.checkedInAt
            from EventApplication ea
            where ea.event.id = :eventId
            and ea.status = com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus.CHECKED_IN
            and ea.checkedInAt is not null
            order by ea.checkedInAt asc
            """)
    List<LocalDateTime> findCheckedInTimesByEventId(@Param("eventId") Long eventId);
}
