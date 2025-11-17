package com.ssasinsa.wearagain.domain.ticket.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.ticket.docs.TicketApiDocs;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeRequest;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketChargeResponse;
import com.ssasinsa.wearagain.domain.ticket.service.TicketAdminService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tickets")
@Tag(name = TicketApiDocs.ADMIN_TAG_NAME, description = TicketApiDocs.ADMIN_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class TicketAdminController {

    private final TicketAdminService ticketAdminService;

    @TicketApiDocs.AdminChargeTicket
    @PostMapping("/charge")
    public ResponseEntity<TicketChargeResponse> chargeTicket(
            @AuthenticationPrincipal AdminAuthenticatedUser principal,
            @Valid @RequestBody TicketChargeRequest request
    ) {
        ensureAuthenticated(principal);
        TicketChargeResponse response = ticketAdminService.chargeTicket(request, principal.adminId());
        return ResponseEntity.ok(response);
    }

    private void ensureAuthenticated(AdminAuthenticatedUser principal) {
        if (principal == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
