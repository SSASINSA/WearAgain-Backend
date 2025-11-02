package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;

public interface EventService {

    EventCreateResponse createEvent(EventCreateRequest request);
}
