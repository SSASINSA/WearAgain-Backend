package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventStaffCodeResponse;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestListResponse;
import java.util.List;

public interface EventAdminService {

    EventCreateResponse createEvent(EventAdminCreateRequest request, Long adminId);

    EventAdminListResponse getEvents(String status, int page, int size);

    EventAdminDetailResponse getEventDetail(Long eventId);

    EventAdminDetailResponse updateEvent(Long eventId, EventAdminUpdateRequest request, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEventStatus(Long eventId, EventStatus status, AdminRole role);

    void archiveEvent(Long eventId);

    EventApplicationRejectResponse rejectApplication(Long applicationId, EventApplicationRejectRequest request);

    EventStaffCodeResponse issueStaffCode(Long eventId, Long adminId);

    EventStaffCodeResponse getStaffCode(Long eventId, Long adminId);

    List<EventApprovalRequestListResponse> getPendingApprovalRequests();

    EventApprovalRequestDetailResponse getApprovalRequestDetail(Long approvalRequestId);

    String approveApprovalRequest(Long eventId, Long superAdminId);

    String rejectApprovalRequest(Long eventId, Long superAdminId);
}
