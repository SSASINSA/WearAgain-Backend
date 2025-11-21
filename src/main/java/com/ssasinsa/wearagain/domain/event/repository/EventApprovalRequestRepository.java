package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventApprovalRequestRepository extends JpaRepository<EventApprovalRequest, Long> {
    List<EventApprovalRequest> findByEventStatusOrderByCreatedAtDesc(EventStatus status);
}
