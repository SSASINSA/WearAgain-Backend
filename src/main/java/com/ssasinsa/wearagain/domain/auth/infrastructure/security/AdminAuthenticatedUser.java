package com.ssasinsa.wearagain.domain.auth.infrastructure.security;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;

public record AdminAuthenticatedUser(
        Long adminId,
        String email,
        String name,
        AdminRole role
) {
}
