package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCodeVerifyRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCodeVerifyResponse;

public interface EventStaffService {

    EventStaffCheckInResponse checkIn(EventStaffCheckInRequest request);

    EventStaffCodeVerifyResponse verifyStaffCode(EventStaffCodeVerifyRequest request);
}
