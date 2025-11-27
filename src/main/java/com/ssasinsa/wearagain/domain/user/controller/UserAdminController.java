package com.ssasinsa.wearagain.domain.user.controller;

import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.service.UserAdminService;
import com.ssasinsa.wearagain.global.dto.MessageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/participants")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    public ResponseEntity<AdminParticipantListResponse> getParticipants(
            @RequestParam(value = "suspended", required = false) Boolean suspended,
            @RequestParam(value = "sortBy", defaultValue = "CREATED_DESC") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        AdminParticipantListResponse response = userAdminService.getParticipants(suspended, sortBy, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{participantId}")
    public ResponseEntity<AdminParticipantDetailResponse> getParticipantDetail(
            @PathVariable Long participantId
    ) {
        AdminParticipantDetailResponse response = userAdminService.getParticipantDetail(participantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminParticipantStatsResponse> getParticipantStats() {
        AdminParticipantStatsResponse response = userAdminService.getParticipantStats();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{participantId}")
    public ResponseEntity<AdminParticipantDetailResponse> updateParticipant(
            @PathVariable Long participantId,
            @Valid @RequestBody AdminParticipantUpdateRequest request
    ) {
        AdminParticipantDetailResponse response = userAdminService.updateParticipant(participantId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{participantId}/suspension")
    public ResponseEntity<MessageResponse> updateSuspension(
            @PathVariable Long participantId,
            @Valid @RequestBody AdminParticipantSuspensionRequest request
    ) {
        AdminParticipantDetailResponse response = userAdminService.updateSuspension(participantId, request);
        return ResponseEntity.ok(MessageResponse.of("참가자 상태가 업데이트되었습니다."));
    }
}
