package com.ssasinsa.wearagain.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.apple")
public record AppleOAuthProperties(
        String clientId,
        String teamId,
        String keyId,
        String privateKey,
        String redirectUri,
        String tokenUri,
        String keyUri
) {
}
