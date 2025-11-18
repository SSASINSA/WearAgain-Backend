package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByStatusIn(Collection<EventStatus> statuses, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status IN :statuses"
            + " AND (:cursor IS NULL OR e.id > :cursor)"
            + " ORDER BY e.startDate ASC, e.id ASC")
    List<Event> findEventsAfterCursor(
            @Param("statuses") Collection<EventStatus> statuses,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    Optional<Event> findByStaffCode(String staffCode);

    List<Event> findByStatusAndScissorGrantedFalse(EventStatus status);
}
