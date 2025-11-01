package com.ssasinsa.wearagain.domain.auth.controller;

import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLogoutRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupApproveRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRejectRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRequestCreateRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminTokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupApprovalResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestListResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSimpleResponse;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.auth.service.AdminAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthTokenResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AdminAuthTokenResponse> refresh(@Valid @RequestBody AdminTokenRefreshRequest request) {
        return ResponseEntity.ok(adminAuthService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<AdminSimpleResponse> logout(@Valid @RequestBody AdminLogoutRequest request) {
        return ResponseEntity.ok(adminAuthService.logout(request));
    }

    @PostMapping("/signup-requests")
    public ResponseEntity<AdminSignupRequestResponse> signup(@Valid @RequestBody AdminSignupRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAuthService.createSignupRequest(request));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/signup-requests")
    public ResponseEntity<AdminSignupRequestListResponse> findSignupRequests(
            @RequestParam(value = "status", required = false) AdminSignupRequestStatus status
    ) {
        return ResponseEntity.ok(adminAuthService.getSignupRequests(status));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/signup-requests/{requestId}/approve")
    public ResponseEntity<AdminSignupApprovalResponse> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal,
            @Valid @RequestBody AdminSignupApproveRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.approveSignupRequest(requestId, principal.adminId(), request));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/signup-requests/{requestId}/reject")
    public ResponseEntity<AdminSimpleResponse> reject(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal,
            @Valid @RequestBody AdminSignupRejectRequest request
    ) {
        return ResponseEntity.ok(adminAuthService.rejectSignupRequest(requestId, principal.adminId(), request));
    }
}
