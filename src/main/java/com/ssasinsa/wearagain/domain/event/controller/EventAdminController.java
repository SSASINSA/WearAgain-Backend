package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCreateRequest;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventImageUploadResponse;
import com.ssasinsa.wearagain.domain.event.service.EventAdminService;
import com.ssasinsa.wearagain.domain.event.service.EventImageUploadService;
import com.ssasinsa.wearagain.domain.event.service.EventService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = EventApiDocs.TAG_NAME, description = EventApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventService eventService;
    private final EventImageUploadService eventImageUploadService;
    private final EventAdminService eventAdminService;

    @EventApiDocs.CreateEvent
    @PostMapping("/events")
    public ResponseEntity<EventCreateResponse> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        EventCreateResponse response = eventService.createEvent(request, principal.adminId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @EventApiDocs.UploadImage
    @PostMapping(value = "/events/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventImageUploadResponse> uploadEventImage(@RequestPart("file") MultipartFile file) {
        String imageName = eventImageUploadService.uploadImage(file);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/upload/")
                .path(imageName)
                .toUriString();
        EventImageUploadResponse response = new EventImageUploadResponse(imageName, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @EventApiDocs.ListAdminEvents
    @GetMapping("/events")
    public ResponseEntity<EventAdminListResponse> getEvents(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(eventAdminService.getEvents(status, page, size));
    }

    @EventApiDocs.GetAdminEventDetail
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventAdminDetailResponse> getEventDetail(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventAdminService.getEventDetail(eventId));
    }

    @EventApiDocs.UpdateEvent
    @PutMapping("/events/{eventId}")
    public ResponseEntity<EventAdminDetailResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventAdminUpdateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        return ResponseEntity.ok(eventAdminService.updateEvent(eventId, request, principal.adminId(), principal.role()));
    }

    @EventApiDocs.UpdateEventStatus
    @PatchMapping("/events/{eventId}/status")
    public ResponseEntity<EventAdminDetailResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody EventAdminStatusUpdateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        return ResponseEntity.ok(eventAdminService.updateEventStatus(eventId, request.status(), principal.adminId(), principal.role()));
    }

    @EventApiDocs.DeleteEvent
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> archiveEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        eventAdminService.archiveEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @EventApiDocs.RejectEventApplication
    @PatchMapping("/applications/{applicationId}/reject")
    public ResponseEntity<EventApplicationRejectResponse> rejectApplication(
            @PathVariable Long applicationId,
            @Valid @RequestBody EventApplicationRejectRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        EventApplicationRejectResponse response = eventAdminService.rejectApplication(applicationId, request);
        return ResponseEntity.ok(response);
    }

    private void ensureAuthenticated(AdminAuthenticatedUser principal) {
        if (principal == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
