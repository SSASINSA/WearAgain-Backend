package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.ssasinsa.wearagain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

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
        WebClient googleClient = WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        try {
            String decodedCode = URLDecoder.decode(authorizationCode, StandardCharsets.UTF_8);
            GoogleOAuthTokenResponse response = googleClient.post()
                    .uri(uriBuilder -> {
                        var builtUri = uriBuilder
                                .path("/token")
                                .queryParam("code", decodedCode)
                                .queryParam("client_id", properties.clientId())
                                .queryParam("client_secret", properties.clientSecret())
                                .queryParam("grant_type", GRANT_TYPE_AUTHORIZATION_CODE)
                                .queryParam("redirect_uri", properties.redirectUri())
                                .build();
                        System.out.println("[GoogleOAuthClient] Actual requestToken URL: " + builtUri);
                        return builtUri;
                    })
                    .retrieve()
                    .onStatus(HttpStatus.BAD_REQUEST::equals,
                            r -> r.bodyToMono(String.class).map(Exception::new))
                    .bodyToMono(GoogleOAuthTokenResponse.class)
                    .doOnError(e -> {
                        System.err.println("[GoogleOAuthClient] Error during token request: " + e.getMessage());
                    })
                    .onErrorMap(e -> new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED, e))
                    .block();
            if (response == null) {
                throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
            }
            return response;
        } catch (Exception exception) {
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
//                    .onStatus(httpStatus -> httpStatus.is2xxSuccessful(), clientResponse -> clientResponse.bodyToMono(String.class).map(Exception::new))
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
