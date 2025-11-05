package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;

public interface EventAdminService {

    EventAdminListResponse getEvents(String status, int page, int size);

    EventAdminDetailResponse getEventDetail(Long eventId);

    EventAdminDetailResponse updateEvent(Long eventId, EventAdminUpdateRequest request, Long adminId, AdminRole role);

    EventAdminDetailResponse updateEventStatus(Long eventId, EventStatus status, Long adminId, AdminRole role);

    void archiveEvent(Long eventId);

    EventApplicationRejectResponse rejectApplication(Long applicationId, EventApplicationRejectRequest request);
}
