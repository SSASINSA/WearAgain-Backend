package com.ssasinsa.wearagain.domain.auth.dto.request;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminSignupApproveRequest(
        @NotNull(message = "부여할 역할을 선택해 주세요.")
        AdminRole role
) {
}
