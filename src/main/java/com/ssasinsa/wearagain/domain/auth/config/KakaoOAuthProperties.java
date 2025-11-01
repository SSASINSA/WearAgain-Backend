package com.ssasinsa.wearagain.domain.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.kakao")
public record KakaoOAuthProperties(
        String clientId,
        String clientSecret,
        String appClientId,
        String redirectUri,
        String tokenUri,
        String userInfoUri,
        String issuer,
        String keyUri
) {
}
