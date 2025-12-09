package com.ssasinsa.wearagain.domain.user.dto.admin;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record AdminManagedUserResponse(
        Long adminUserId,
        String email,
        String name,
        AdminRole role,
        AdminStatus status,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static AdminManagedUserResponse from(AdminUser adminUser) {
        return new AdminManagedUserResponse(
                adminUser.getId(),
                adminUser.getEmail(),
                adminUser.getName(),
                adminUser.getRole(),
                adminUser.getStatus(),
                adminUser.getLastLoginAt() == null ? null : adminUser.getLastLoginAt().atOffset(ZoneOffset.UTC),
                adminUser.getCreatedAt() == null ? null : adminUser.getCreatedAt().atOffset(ZoneOffset.UTC),
                adminUser.getUpdatedAt() == null ? null : adminUser.getUpdatedAt().atOffset(ZoneOffset.UTC)
        );
    }
}
