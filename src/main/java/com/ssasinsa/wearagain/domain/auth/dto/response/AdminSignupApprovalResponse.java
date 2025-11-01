package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;

public record AdminSignupApprovalResponse(
        Long adminUserId,
        String email,
        AdminRole role,
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
