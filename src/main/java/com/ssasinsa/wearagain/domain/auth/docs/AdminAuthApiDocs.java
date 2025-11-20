package com.ssasinsa.wearagain.domain.auth.docs;

import com.ssasinsa.wearagain.domain.auth.dto.response.AdminAuthTokenResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupApprovalResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestListResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSignupRequestResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminSimpleResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.AdminRoleResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public final class AdminAuthApiDocs {

    private AdminAuthApiDocs() {
    }

    public static final String TAG_NAME = "Admin Auth API";
    public static final String TAG_DESCRIPTION = "관리자 로그인 및 가입 승인/거절 API";

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 로그인",
            description = "관리자 계정으로 로그인하여 Access/Refresh Token을 발급합니다.",
            requestExample = AdminAuthExamples.ADMIN_LOGIN_REQUEST,
            responseSchema = AdminAuthTokenResponse.class,
            responseExample = AdminAuthExamples.ADMIN_LOGIN_RESPONSE
    )
    public @interface Login {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 토큰 재발급",
            description = "관리자 Refresh Token을 검증하여 새로운 토큰을 발급합니다.",
            requestExample = AdminAuthExamples.ADMIN_REFRESH_REQUEST,
            responseSchema = AdminAuthTokenResponse.class,
            responseExample = AdminAuthExamples.ADMIN_LOGIN_RESPONSE
    )
    public @interface Refresh {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 로그아웃",
            description = "Refresh Token을 폐기하여 관리자 세션을 만료시킵니다.",
            requestExample = AdminAuthExamples.ADMIN_REFRESH_REQUEST,
            responseSchema = AdminSimpleResponse.class,
            responseExample = AdminAuthExamples.ADMIN_SIMPLE_RESPONSE
    )
    @SecurityRequirement(name = "adminJWT")
    public @interface Logout {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 가입 신청",
            description = "일반 관리자 계정이 가입 승인을 요청합니다.",
            requestExample = AdminAuthExamples.ADMIN_SIGNUP_REQUEST,
            responseSchema = AdminSignupRequestResponse.class,
            responseExample = AdminAuthExamples.ADMIN_SIGNUP_RESPONSE
    )
    public @interface SignupRequest {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 가입 신청 목록 조회",
            description = "SUPER_ADMIN 권한이 가입 신청 목록을 상태별로 조회합니다.",
            responseSchema = AdminSignupRequestListResponse.class,
            responseExample = AdminAuthExamples.ADMIN_SIGNUP_LIST_RESPONSE
    )
    @SecurityRequirement(name = "adminJWT")
    public @interface SignupRequestList {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 가입 승인",
            description = "SUPER_ADMIN이 가입 신청을 승인하여 관리자 계정을 생성합니다.",
            responseSchema = AdminSignupApprovalResponse.class,
            responseExample = AdminAuthExamples.ADMIN_APPROVE_RESPONSE
    )
    @SecurityRequirement(name = "adminJWT")
    public @interface ApproveSignup {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "관리자 가입 거절",
            description = "SUPER_ADMIN이 가입 신청을 거절합니다.",
            responseSchema = AdminSimpleResponse.class,
            responseExample = AdminAuthExamples.ADMIN_SIMPLE_RESPONSE
    )
    @SecurityRequirement(name = "adminJWT")
    public @interface RejectSignup {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "내 권한 조회",
            description = "현재 로그인한 관리자의 권한(Role)을 조회합니다.",
            responseSchema = AdminRoleResponse.class,
            responseExample = AdminAuthExamples.ADMIN_MY_ROLE_RESPONSE
    )
    @SecurityRequirement(name = "adminJWT")
    public @interface GetMyRole {
    }
}
