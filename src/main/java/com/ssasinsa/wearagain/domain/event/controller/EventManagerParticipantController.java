package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSort;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.service.EventParticipantManagerService;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class EventManagerParticipantController {

    private final EventParticipantManagerService eventParticipantManagerService;

    @GetMapping("/event-applications")
    public ResponseEntity<ManagerEventParticipantListResponse> getParticipants(
            @AuthenticationPrincipal AdminAuthenticatedUser principal,
            @RequestParam(value = "eventId") Long eventId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "suspended", required = false) Boolean suspended,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "keywordScope", required = false) String keywordScope,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        ManagerEventParticipantListResponse response = eventParticipantManagerService.getParticipants(
                principal,
                eventId,
                resolveStatus(status),
                suspended,
                keyword,
                ManagerEventParticipantKeywordScope.from(keywordScope),
                page,
                size,
                ManagerEventParticipantSort.from(sort)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/{eventId}/applications/{applicationId}")
    public ResponseEntity<AdminParticipantDetailResponse> getParticipantDetail(
            @PathVariable Long eventId,
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        AdminParticipantDetailResponse response =
                eventParticipantManagerService.getParticipantDetail(eventId, applicationId, principal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/events/{eventId}/applications/{applicationId}/cancel")
    public ResponseEntity<Void> cancelApplication(
            @PathVariable Long eventId,
            @PathVariable Long applicationId,
            @Valid @RequestBody ManagerEventParticipantCancelRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        eventParticipantManagerService.cancelApplication(eventId, applicationId, request, principal);
        return ResponseEntity.noContent().build();
    }

    private EventApplicationStatus resolveStatus(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return EventApplicationStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new EventException(EventErrorCode.INVALID_EVENT_QUERY, exception);
        }
    }
}
