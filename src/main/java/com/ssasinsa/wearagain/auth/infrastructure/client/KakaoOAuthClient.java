package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.ssasinsa.wearagain.auth.config.KakaoOAuthProperties;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class KakaoOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final WebClient webClient;
    private final KakaoOAuthProperties properties;

    public KakaoOAuthClient(WebClient.Builder webClientBuilder, KakaoOAuthProperties properties) {
        this.webClient = webClientBuilder.build();
        this.properties = properties;
    }

    public KakaoOAuthTokenResponse requestToken(String authorizationCode) {
        try {
            KakaoOAuthTokenResponse response = webClient.post()
                    .uri(properties.tokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
                            .with("client_id", properties.clientId())
                            .with("client_secret", properties.clientSecret())
                            .with("redirect_uri", properties.redirectUri())
                            .with("code", authorizationCode))
                    .retrieve()
                    .bodyToMono(KakaoOAuthTokenResponse.class)
                    .doOnError(error -> log.error("Failed to request Kakao token: {}", error.getMessage()))
                    .onErrorMap(error -> new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED, error))
                    .block();
            if (response == null || response.accessToken() == null) {
                throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED);
            }
            return response;
        } catch (AuthException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuthException(AuthErrorCode.KAKAO_TOKEN_REQUEST_FAILED, exception);
        }
    }

    public KakaoUserInfoResponse fetchUserInfo(String accessToken) {
        try {
            KakaoUserInfoResponse response = webClient.get()
                    .uri(properties.userInfoUri())
                    .headers(headers -> {
                        headers.setBearerAuth(accessToken);
                        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                    })
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponse.class)
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED);
            }
            return response;
        } catch (WebClientResponseException | WebClientRequestException exception) {
            log.error("Failed to fetch Kakao user info: {}", exception.getMessage());
            throw new AuthException(AuthErrorCode.KAKAO_USERINFO_REQUEST_FAILED, exception);
        }
    }
}
