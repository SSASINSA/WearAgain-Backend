package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 가입 승인 응답")
public record AdminSignupApprovalResponse(
        @Schema(description = "생성된 관리자 ID", example = "5")
        Long adminUserId,

        @Schema(description = "관리자 이메일", example = "admin@wearagain.kr")
        String email,

        @Schema(description = "부여된 역할", example = "ADMIN")
        AdminRole role,

        @Schema(description = "관리자 계정 상태", example = "ACTIVE")
        String status
) {

    public static AdminSignupApprovalResponse of(AdminUser adminUser) {
        return new AdminSignupApprovalResponse(
                adminUser.getId(),
                adminUser.getEmail(),
                adminUser.getRole(),
                adminUser.getStatus().name()
        );
    }
}
