package com.ssasinsa.wearagain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.redis")
public record AuthRedisProperties(String refreshTokenPrefix) {
}
