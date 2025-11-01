package com.ssasinsa.wearagain.domain.auth.service;

import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminLogoutRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupApproveRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRejectRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminSignupRequestCreateRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.AdminTokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupApprovalResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSimpleResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestListResponse;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;

public interface AdminAuthService {

    AdminAuthTokenResponse login(AdminLoginRequest request);

    AdminAuthTokenResponse refresh(AdminTokenRefreshRequest request);

    AdminSimpleResponse logout(AdminLogoutRequest request);

    AdminSignupRequestResponse createSignupRequest(AdminSignupRequestCreateRequest request);

    AdminSignupApprovalResponse approveSignupRequest(Long requestId, Long reviewerId, AdminSignupApproveRequest request);

    AdminSimpleResponse rejectSignupRequest(Long requestId, Long reviewerId, AdminSignupRejectRequest request);

    AdminSignupRequestListResponse getSignupRequests(AdminSignupRequestStatus status);
}
