package com.ssasinsa.wearagain.domain.auth.controller;

import com.ssasinsa.wearagain.domain.auth.docs.AdminAuthApiDocs;
import com.ssasinsa.wearagain.domain.auth.dto.request.*;
import com.ssasinsa.wearagain.domain.auth.dto.response.*;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.auth.service.AdminAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = AdminAuthApiDocs.TAG_NAME, description = AdminAuthApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @AdminAuthApiDocs.Login
    @PostMapping("/login")
    public ResponseEntity<AdminAuthTokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @AdminAuthApiDocs.Refresh
    @PostMapping("/refresh")
    public ResponseEntity<AdminAuthTokenResponse> refresh(@Valid @RequestBody AdminTokenRefreshRequest request) {
        return ResponseEntity.ok(adminAuthService.refresh(request));
    }

    @AdminAuthApiDocs.Logout
    @PostMapping("/logout")
    public ResponseEntity<AdminSimpleResponse> logout(@Valid @RequestBody AdminLogoutRequest request) {
        return ResponseEntity.ok(adminAuthService.logout(request));
    }

    @AdminAuthApiDocs.SignupRequest
    @PostMapping("/signup-requests")
    public ResponseEntity<AdminSignupRequestResponse> signup(@Valid @RequestBody AdminSignupRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAuthService.createSignupRequest(request));
    }

    @AdminAuthApiDocs.SignupRequestList
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/signup-requests")
    public ResponseEntity<AdminSignupRequestListResponse> findSignupRequests(
            @RequestParam(value = "status", required = false) AdminSignupRequestStatus status
    ) {
        return ResponseEntity.ok(adminAuthService.getSignupRequests(status));
    }

    @AdminAuthApiDocs.ApproveSignup
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/signup-requests/{requestId}/approve")
    public ResponseEntity<AdminSignupApprovalResponse> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        return ResponseEntity.ok(adminAuthService.approveSignupRequest(requestId, principal.adminId()));
    }

    @AdminAuthApiDocs.RejectSignup
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/signup-requests/{requestId}/reject")
    public ResponseEntity<AdminSimpleResponse> reject(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        return ResponseEntity.ok(adminAuthService.rejectSignupRequest(requestId, principal.adminId()));
    }
}
