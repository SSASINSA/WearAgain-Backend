package com.ssasinsa.wearagain.domain.ticket.controller;

import com.ssasinsa.wearagain.domain.ticket.docs.TicketApiDocs;
import com.ssasinsa.wearagain.domain.ticket.dto.TicketQrResponse;
import com.ssasinsa.wearagain.domain.ticket.service.TicketService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = TicketApiDocs.USER_TAG_NAME, description = TicketApiDocs.USER_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @TicketApiDocs.GetTicketQr
    @GetMapping("/qr")
    public ResponseEntity<TicketQrResponse> getTicketQr(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        TicketQrResponse response = ticketService.getTicketQr(user.userId());
        return ResponseEntity.ok(response);
    }
}
