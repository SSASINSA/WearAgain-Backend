package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.event.dto.admin.*;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.global.dto.MessageResponse;

import java.util.List;

public interface EventAdminService {

    EventCreateResponse createEvent(EventAdminCreateRequest request, Long adminId);

    EventAdminListResponse getEvents(
            String status,
            int page,
            int size,
            Long adminId,
            AdminRole role,
            String keyword,
            String keywordScope
    );

    EventAdminDetailResponse getEventDetail(Long eventId, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEvent(Long eventId, EventAdminUpdateRequest request, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEventStatus(Long eventId, EventStatus status, AdminRole role);

    void archiveEvent(Long eventId);

    EventApplicationRejectResponse rejectApplication(Long applicationId, EventApplicationRejectRequest request);

    EventStaffCodeResponse issueStaffCode(Long eventId, Long adminId);

    EventStaffCodeResponse getStaffCode(Long eventId, Long adminId);

    List<EventApprovalRequestListResponse> getPendingApprovalRequests();

    EventApprovalRequestDetailResponse getApprovalRequestDetail(Long approvalRequestId);

    MessageResponse approveApprovalRequest(Long approvalRequestId, Long adminId);

    MessageResponse rejectApprovalRequest(Long approvalRequestId, Long adminId);
}
