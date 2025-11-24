package com.ssasinsa.wearagain.domain.event.controller;

import com.ssasinsa.wearagain.domain.event.docs.EventApiDocs;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminCreateRequest;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventStaffCodeResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventImageUploadResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApprovalRequestListResponse;
import com.ssasinsa.wearagain.domain.event.service.EventAdminService;
import com.ssasinsa.wearagain.domain.event.service.EventImageUploadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class EventAdminController {

    private final EventImageUploadService eventImageUploadService;
    private final EventAdminService eventAdminService;

    @EventApiDocs.CreateEvent
    @PostMapping("/events")
    public ResponseEntity<EventCreateResponse> createEvent(
            @Valid @RequestBody EventAdminCreateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        EventCreateResponse response = eventAdminService.createEvent(request, principal.adminId());
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
        return ResponseEntity.ok(eventAdminService.updateEvent(eventId, request, principal.adminId(), principal.role()));
    }

    @EventApiDocs.UpdateEventStatus
    @PatchMapping("/events/{eventId}/status")
    public ResponseEntity<EventAdminDetailResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody EventAdminStatusUpdateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        return ResponseEntity.ok(eventAdminService.updateEventStatus(eventId, request.status(), principal.role()));
    }

    @EventApiDocs.IssueStaffCode
    @PostMapping("/events/{eventId}/staff-code")
    public ResponseEntity<EventStaffCodeResponse> issueStaffCode(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        EventStaffCodeResponse response = eventAdminService.issueStaffCode(eventId, principal.adminId());
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.GetStaffCode
    @GetMapping("/events/{eventId}/staff-code")
    public ResponseEntity<EventStaffCodeResponse> getStaffCode(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        EventStaffCodeResponse response = eventAdminService.getStaffCode(eventId, principal.adminId());
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.DeleteEvent
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> archiveEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
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
        EventApplicationRejectResponse response = eventAdminService.rejectApplication(applicationId, request);
        return ResponseEntity.ok(response);
    }

    @EventApiDocs.ApproveApprovalRequest
    @PostMapping("/events/approvals/{approvalRequestId}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> approveApprovalRequest(
            @PathVariable Long approvalRequestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        String response = eventAdminService.approveApprovalRequest(
                approvalRequestId,
                principal.adminId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @EventApiDocs.RejectApprovalRequest
    @PostMapping("/events/approvals/{approvalRequestId}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> rejectApprovalRequest(
            @PathVariable Long approvalRequestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        String response = eventAdminService.rejectApprovalRequest(
                approvalRequestId,
                principal.adminId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @EventApiDocs.ListPendingApprovals
    @GetMapping("/events/approvals")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<java.util.List<EventApprovalRequestListResponse>> getPendingApprovals() {
        java.util.List<EventApprovalRequestListResponse> responses = eventAdminService.getPendingApprovalRequests();
        return ResponseEntity.ok(responses);
    }

    @EventApiDocs.GetApprovalDetail
    @GetMapping("/events/approvals/{approvalRequestId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<EventApprovalRequestDetailResponse> getApprovalDetail(
            @PathVariable Long approvalRequestId
    ) {
        EventApprovalRequestDetailResponse response = eventAdminService.getApprovalRequestDetail(approvalRequestId);
        return ResponseEntity.ok(response);
    }
}
