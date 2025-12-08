package com.ssasinsa.wearagain.domain.user.controller;

import com.ssasinsa.wearagain.domain.user.docs.AdminParticipantApiDocs;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserListResponse;
import com.ssasinsa.wearagain.domain.user.service.AdminManagedUserService;
import com.ssasinsa.wearagain.domain.user.service.UserAdminService;
import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserKeywordScope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@RequiredArgsConstructor
@Tag(name = AdminParticipantApiDocs.TAG_NAME, description = AdminParticipantApiDocs.TAG_DESCRIPTION)
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final AdminManagedUserService adminManagedUserService;

    @AdminParticipantApiDocs.GetParticipants
    @GetMapping("/participants")
    public ResponseEntity<AdminParticipantListResponse> getParticipants(
            @RequestParam(value = "suspended", required = false) Boolean suspended,
            @RequestParam(value = "sortBy", defaultValue = "CREATED_DESC") String sortBy,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "keywordScope", required = false) String keywordScope,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        AdminParticipantListResponse response = userAdminService.getParticipants(
                suspended,
                sortBy,
                keyword,
                AdminParticipantKeywordScope.from(keywordScope),
                pageable
        );
        return ResponseEntity.ok(response);
    }

    @AdminParticipantApiDocs.GetParticipantDetail
    @GetMapping("/participants/{participantId}")
    public ResponseEntity<AdminParticipantDetailResponse> getParticipantDetail(
            @PathVariable Long participantId
    ) {
        AdminParticipantDetailResponse response = userAdminService.getParticipantDetail(participantId);
        return ResponseEntity.ok(response);
    }

    @AdminParticipantApiDocs.GetParticipantStats
    @GetMapping("/participants/stats")
    public ResponseEntity<AdminParticipantStatsResponse> getParticipantStats() {
        AdminParticipantStatsResponse response = userAdminService.getParticipantStats();
        return ResponseEntity.ok(response);
    }

    @AdminParticipantApiDocs.UpdateParticipantBalance
    @PutMapping("/participants/{participantId}")
    public ResponseEntity<AdminParticipantDetailResponse> updateParticipant(
            @PathVariable Long participantId,
            @Valid @RequestBody AdminParticipantUpdateRequest request
    ) {
        AdminParticipantDetailResponse response = userAdminService.updateParticipant(participantId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/participants/{participantId}/suspension")
    @AdminParticipantApiDocs.UpdateSuspension
    public ResponseEntity<AdminParticipantDetailResponse> updateSuspension(
            @PathVariable Long participantId,
            @Valid @RequestBody AdminParticipantSuspensionRequest request
    ) {
        AdminParticipantDetailResponse response = userAdminService.updateSuspension(participantId, request);
        return ResponseEntity.ok(response);
    }

    @AdminParticipantApiDocs.GetAdminUsers
    @GetMapping("/admin-users")
    public ResponseEntity<AdminManagedUserListResponse> getAdminUsers(
            @RequestParam(value = "status", required = false) AdminStatus status,
            @RequestParam(value = "sortBy", defaultValue = "CREATED_DESC") String sortBy,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "keywordScope", required = false) String keywordScope,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        AdminManagedUserListResponse response = adminManagedUserService.getAdminUsers(
                status,
                keyword,
                AdminManagedUserKeywordScope.from(keywordScope),
                page,
                size,
                sortBy
        );
        return ResponseEntity.ok(response);
    }

    @AdminParticipantApiDocs.DeleteAdminUser
    @DeleteMapping("/admin-users/{adminUserId}")
    public ResponseEntity<Void> deleteAdminUser(
            @PathVariable Long adminUserId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        adminManagedUserService.softDeleteAdminUser(adminUserId, principal.adminId());
        return ResponseEntity.noContent().build();
    }
}
