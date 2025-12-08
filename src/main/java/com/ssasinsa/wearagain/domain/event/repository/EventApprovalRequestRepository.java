package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventApprovalRequestRepository extends JpaRepository<EventApprovalRequest, Long>,
        JpaSpecificationExecutor<EventApprovalRequest> {

    List<EventApprovalRequest> findByEvent_StatusAndProcessedAtIsNullOrderByCreatedAtDesc(EventStatus status);

    Optional<EventApprovalRequest> findByEvent_Id(Long eventId);

    @Query("""
            select request from EventApprovalRequest request
            where request.processedAt is null
              and request.event.status = :status
              and request.event.endDate < :targetDate
            """)
    List<EventApprovalRequest> findExpiredPendingRequests(
            @Param("status") EventStatus status,
            @Param("targetDate") LocalDate targetDate
    );
}
