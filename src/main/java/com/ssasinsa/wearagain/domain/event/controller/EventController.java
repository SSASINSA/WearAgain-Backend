package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationQrResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;
import com.ssasinsa.wearagain.domain.event.service.EventUserService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/events")
@Tag(name = EventApiDocs.USER_TAG_NAME, description = EventApiDocs.USER_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class EventController {

    private final EventUserService eventUserService;

    @EventApiDocs.ListEvents
    @GetMapping
    public ResponseEntity<EventListResponse> getEvents(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        EventListResponse response = eventUserService.getEvents(status, cursor, size);
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.GetEventDetail
    @GetMapping("/{eventId}")
    public ResponseEntity<EventDetailResponse> getEventDetail(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        EventDetailResponse response = eventUserService.getEventDetail(eventId, user != null ? user.userId() : null);
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.ListUserApplications
    @GetMapping("/applications")
    public ResponseEntity<EventApplicationListResponse> getApplications(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(name = "status", required = false) EventApplicationStatus status,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "limit", defaultValue = "20") int limit
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        EventApplicationListResponse response = eventUserService.getUserApplications(
                user.userId(),
                status,
                from,
                to,
                cursor,
                limit
        );
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.GetUserApplicationDetail
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<EventApplicationDetailResponse> getApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        EventApplicationDetailResponse response = eventUserService.getUserApplicationDetail(applicationId, user.userId());
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.ApplyEvent
    @PostMapping("/{eventId}/apply")
    public ResponseEntity<EventApplyResponse> applyEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventApplyRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        EventApplyResponse response = eventUserService.apply(eventId, request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @EventApiDocs.IssueApplicationQr
    @PostMapping("/applications/{applicationId}/qr")
    public ResponseEntity<EventApplicationQrResponse> issueQr(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        EventApplicationQrResponse response = eventUserService.issueApplicationQr(applicationId, user.userId());
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.CancelEvent
    @PatchMapping("/applications/{applicationId}/cancel")
    public ResponseEntity<EventCancelResponse> cancelApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody EventCancelRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        EventCancelResponse response = eventUserService.cancel(applicationId, request, user.userId());
        return ResponseEntity.ok(response);
    }
}
