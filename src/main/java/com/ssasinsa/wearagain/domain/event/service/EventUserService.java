package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationQrResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import java.time.LocalDate;

public interface EventUserService {

    EventListResponse getEvents(String status, String cursor, int size);

    EventDetailResponse getEventDetail(Long eventId, Long userId);

    EventApplyResponse apply(Long eventId, EventApplyRequest request, Long userId);

    EventCancelResponse cancel(Long applicationId, EventCancelRequest request, Long userId);

    EventApplicationListResponse getUserApplications(
            Long userId,
            EventApplicationStatus[] statuses,
            LocalDate from,
            LocalDate to,
            String cursor,
            int size
    );

    EventApplicationQrResponse issueApplicationQr(Long applicationId, Long userId);

    EventApplicationDetailResponse getUserApplicationDetail(Long applicationId, Long userId);
}
