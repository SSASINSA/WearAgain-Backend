package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventApplicationRepository extends JpaRepository<EventApplication, Long> {

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
}
