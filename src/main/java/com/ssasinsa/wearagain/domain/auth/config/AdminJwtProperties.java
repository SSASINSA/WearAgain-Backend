package com.ssasinsa.wearagain.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin.jwt")
public record AdminJwtProperties(
        String issuer,
        TokenProperties accessToken,
        TokenProperties refreshToken
) {

    public record TokenProperties(String secret, long validity) {
    }
}
