package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOptionRepository extends JpaRepository<EventOption, Long> {

    Optional<EventOption> findByIdAndEventId(Long eventOptionId, Long eventId);
}
