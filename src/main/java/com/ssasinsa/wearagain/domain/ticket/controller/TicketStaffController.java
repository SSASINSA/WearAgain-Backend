package com.ssasinsa.wearagain.domain.ticket.controller;

import com.ssasinsa.wearagain.domain.ticket.docs.TicketApiDocs;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketStaffChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketUseResponse;
import com.ssasinsa.wearagain.domain.ticket.service.TicketStaffService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/staff/tickets")
@Tag(name = TicketApiDocs.STAFF_TAG_NAME, description = TicketApiDocs.STAFF_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class TicketStaffController {

    private final TicketStaffService ticketStaffService;

    @TicketApiDocs.StaffUseTicket
    @PostMapping("/use")
    public ResponseEntity<TicketUseResponse> useTicket(@Valid @RequestBody TicketUseRequest request) {
        TicketUseResponse response = ticketStaffService.useTicket(request);
        return ResponseEntity.ok(response);
    }

    @TicketApiDocs.StaffChargeTicket
    @PostMapping("/charge")
    public ResponseEntity<TicketChargeResponse> chargeTicket(@Valid @RequestBody TicketStaffChargeRequest request) {
        TicketChargeResponse response = ticketStaffService.chargeTicket(request);
        return ResponseEntity.ok(response);
    }
}
