package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            select e from Event e
            where e.status in :statuses
            and (:cursor is null or e.id > :cursor)
            order by e.startDate asc, e.id asc
            """)
    List<Event> findEventsAfterCursor(
            @Param("statuses") Collection<EventStatus> statuses,
            @Param("cursor") Long cursor,
            Pageable pageable
    );
}
