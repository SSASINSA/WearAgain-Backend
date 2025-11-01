package com.ssasinsa.wearagain.domain.auth.infrastructure.client;

import com.ssasinsa.wearagain.domain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.domain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.domain.auth.exception.AuthException;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final WebClient webClient;
    private final GoogleOAuthProperties properties;

    public GoogleOAuthClient(WebClient.Builder webClientBuilder, GoogleOAuthProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public GoogleOAuthTokenResponse requestToken(String authorizationCode) {
        try {
            String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);
            URI uri = UriComponentsBuilder
                    .fromUriString(properties.tokenUri()) // 절대 URL
                    .queryParam("code", decodedCode)
                    .queryParam("client_id", properties.clientId())
                    .queryParam("client_secret", properties.clientSecret())
                    .queryParam("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
                    .queryParam("redirect_uri", properties.redirectUri())
                    .build(true) // true → 이미 인코딩된 값 유지
                    .toUri();
            GoogleOAuthTokenResponse response = webClient.post()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(GoogleOAuthTokenResponse.class)
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED, exception);
        }
    }

    public GoogleUserInfoResponse fetchUserInfo(String accessToken) {
        try {
            GoogleUserInfoResponse response = webClient.get()
                    .uri(properties.userInfoUri())
                    .headers(httpHeaders -> {
                        httpHeaders.setBearerAuth(accessToken);
                        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .bodyToMono(GoogleUserInfoResponse.class)
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED, exception);
        }
    }
}
