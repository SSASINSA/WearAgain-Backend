package com.ssasinsa.wearagain.domain.auth.dto.request;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "관리자 가입 승인 요청")
public record AdminSignupApproveRequest(
        @Schema(description = "부여할 관리자 역할", example = "ADMIN")
        @NotNull(message = "부여할 역할을 선택해 주세요.")
        AdminRole role
) {
}
