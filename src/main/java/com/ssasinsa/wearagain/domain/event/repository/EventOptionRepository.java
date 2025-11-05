package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {

    Optional<EventOption> findByIdAndEventId(Long eventOptionId, Long eventId);

    @Query("""
            select new com.ssasinsa.wearagain.domain.event.repository.EventCapacitySummary(
                o.event.id,
                sum(o.capacity)
            )
            from EventOption o
            where o.event.id in :eventIds
            and o.capacity is not null
            group by o.event.id
            """)
    List<EventCapacitySummary> sumCapacityByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
