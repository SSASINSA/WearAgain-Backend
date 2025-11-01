package com.ssasinsa.wearagain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String issuer,
        TokenProperties accessToken,
        TokenProperties refreshToken
) {

    public record TokenProperties(String secret, long validity) {
    }
}
