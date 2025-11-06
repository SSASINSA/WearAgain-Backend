package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;

public interface EventStaffService {

    EventStaffCheckInResponse checkIn(EventStaffCheckInRequest request);
}
