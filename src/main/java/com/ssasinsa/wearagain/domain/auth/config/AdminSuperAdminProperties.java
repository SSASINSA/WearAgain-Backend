package com.ssasinsa.wearagain.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.super")
public record AdminSuperAdminProperties(
        String email,
        String password,
        String name
) {
}
