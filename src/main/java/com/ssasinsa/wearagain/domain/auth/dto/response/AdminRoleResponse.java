package com.ssasinsa.wearagain.domain.auth.dto.response;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import lombok.Builder;

@Builder
public record AdminRoleResponse(
        AdminRole role
) {
    public static AdminRoleResponse of(AdminRole role) {
        return AdminRoleResponse.builder()
                .role(role)
                .build();
    }
}

