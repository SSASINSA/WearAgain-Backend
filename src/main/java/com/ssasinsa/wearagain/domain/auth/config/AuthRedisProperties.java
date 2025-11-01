package com.ssasinsa.wearagain.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.redis")
public record AuthRedisProperties(String refreshTokenPrefix) {
}
