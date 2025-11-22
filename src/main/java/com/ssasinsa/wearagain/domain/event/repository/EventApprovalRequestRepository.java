package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventApprovalRequestRepository extends JpaRepository<EventApprovalRequest, Long> {
    List<EventApprovalRequest> findByEventStatusOrderByCreatedAtDesc(EventStatus status);
}
