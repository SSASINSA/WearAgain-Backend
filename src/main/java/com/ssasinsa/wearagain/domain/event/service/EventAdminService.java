package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.event.dto.admin.*;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestPageResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.global.dto.MessageResponse;

public interface EventAdminService {

    EventCreateResponse createEvent(EventAdminCreateRequest request, Long adminId, AdminRole role);

    EventAdminListResponse getEvents(
            String status,
            int page,
            int size,
            String sort,
            Long adminId,
            AdminRole role,
            String keyword,
            String keywordScope
    );

    EventAdminDetailResponse getEventDetail(Long eventId, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEvent(Long eventId, EventAdminUpdateRequest request, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEventStatus(Long eventId, EventStatus status, AdminRole role);

    void archiveEvent(Long eventId);

    EventStaffCodeResponse issueStaffCode(Long eventId, Long adminId);

    EventStaffCodeResponse getStaffCode(Long eventId, Long adminId);

    EventApprovalRequestPageResponse getPendingApprovalRequests(
            int page,
            int size,
            String sort,
            String keyword,
            String keywordScope
    );

    EventApprovalRequestDetailResponse getApprovalRequestDetail(Long approvalRequestId);

    MessageResponse approveApprovalRequest(Long approvalRequestId, Long adminId);

    MessageResponse rejectApprovalRequest(Long approvalRequestId, Long adminId);
}
