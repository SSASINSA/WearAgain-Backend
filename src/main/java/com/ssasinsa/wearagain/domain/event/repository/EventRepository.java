package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    @Query("SELECT e FROM Event e WHERE e.status IN :statuses"
            + " AND (:cursor IS NULL OR e.id > :cursor)"
            + " ORDER BY e.startDate ASC, e.id ASC")
    List<Event> findEventsAfterCursor(
            @Param("statuses") Collection<EventStatus> statuses,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    Optional<Event> findByStaffCode(String staffCode);

    List<Event> findAllByStaffCodeIn(Collection<String> staffCodes);

    List<Event> findByStatusAndScissorGrantedFalse(EventStatus status);

    @Query("SELECT e FROM Event e WHERE e.status <> :closedStatus AND e.endDate < :targetDate")
    List<Event> findEventsToClose(
            @Param("closedStatus") EventStatus closedStatus,
            @Param("targetDate") java.time.LocalDate targetDate
    );

    @Query("SELECT e FROM Event e WHERE e.status = :approvalStatus"
            + " AND e.startDate <= :targetDate"
            + " AND e.endDate >= :targetDate")
    List<Event> findApprovedEventsToOpen(
            @Param("approvalStatus") EventStatus approvalStatus,
            @Param("targetDate") java.time.LocalDate targetDate
    );

    @Query("""
            select distinct e
            from Event e
            left join fetch e.organizerAdmin oa
            left join fetch e.approvalRequest ar
            left join fetch ar.requestingAdmin
            left join fetch ar.processedByAdmin
            left join fetch e.options o1
            where e.id = :id
            """)
    Optional<Event> findWithDetailsById(@Param("id") Long id);

    @Override
    @EntityGraph(attributePaths = {
            "organizerAdmin",
            "approvalRequest",
            "approvalRequest.requestingAdmin",
            "approvalRequest.processedByAdmin"
    })
    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
