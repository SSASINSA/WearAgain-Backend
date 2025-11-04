package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;

public interface EventUserService {

    EventListResponse getEvents(String status, String cursor, int size);

    EventDetailResponse getEventDetail(Long eventId);

    EventApplyResponse apply(Long eventId, EventApplyRequest request, Long userId);

    EventCancelResponse cancel(Long applicationId, EventCancelRequest request, Long userId);
}
