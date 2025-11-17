package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCodeVerifyRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCodeVerifyResponse;
import com.ssasinsa.wearagain.domain.event.service.EventStaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff")
@Tag(name = EventApiDocs.STAFF_TAG_NAME, description = EventApiDocs.STAFF_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class EventStaffController {

    private final EventStaffService eventStaffService;

    @EventApiDocs.StaffCheckIn
    @PostMapping("/events/check-in")
    public ResponseEntity<EventStaffCheckInResponse> checkIn(
            @Valid @RequestBody EventStaffCheckInRequest request
    ) {
        EventStaffCheckInResponse response = eventStaffService.checkIn(request);
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.StaffVerifyCode
    @PostMapping("/codes/verify")
    public ResponseEntity<EventStaffCodeVerifyResponse> verify(
            @Valid @RequestBody EventStaffCodeVerifyRequest request
    ) {
        EventStaffCodeVerifyResponse response = eventStaffService.verifyStaffCode(request);
        return ResponseEntity.ok(response);
    }
}
