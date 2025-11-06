package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInRequest;
import com.ssasinsa.wearagain.domain.event.dto.staff.EventStaffCheckInResponse;
import com.ssasinsa.wearagain.domain.event.service.EventStaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff/events")
@RequiredArgsConstructor
public class EventStaffController {

    private final EventStaffService eventStaffService;

    @EventApiDocs.StaffCheckIn
    @PostMapping("/check-in")
    public ResponseEntity<EventStaffCheckInResponse> checkIn(
            @Valid @RequestBody EventStaffCheckInRequest request
    ) {
        EventStaffCheckInResponse response = eventStaffService.checkIn(request);
        return ResponseEntity.ok(response);
    }
}
