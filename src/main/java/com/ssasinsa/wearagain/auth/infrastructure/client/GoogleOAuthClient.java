package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.ssasinsa.wearagain.auth.config.GoogleOAuthProperties;
import com.ssasinsa.wearagain.auth.exception.AuthErrorCode;
import com.ssasinsa.wearagain.auth.exception.AuthException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthClient {

    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    private final RestTemplate restTemplate;
    private final GoogleOAuthProperties properties;

    public GoogleOAuthClient(RestTemplateBuilder restTemplateBuilder, GoogleOAuthProperties properties) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
    }

    public GoogleOAuthTokenResponse requestToken(String authorizationCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authorizationCode);
        formData.add("client_id", properties.clientId());
        formData.add("client_secret", properties.clientSecret());
        formData.add("redirect_uri", properties.redirectUri());
        formData.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

        try {
            ResponseEntity<GoogleOAuthTokenResponse> response = restTemplate.postForEntity(
                    properties.tokenUri(),
                    entity,
                    GoogleOAuthTokenResponse.class
            );
            GoogleOAuthTokenResponse body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED);
            }
            return body;
        } catch (RestClientException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_TOKEN_REQUEST_FAILED, exception);
        }
    }

    public GoogleUserInfoResponse fetchUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(
                    properties.userInfoUri(),
                    HttpMethod.GET,
                    entity,
                    GoogleUserInfoResponse.class
            );
            GoogleUserInfoResponse body = response.getBody();
            if (!response.getStatusCode().is2xxSuccessful() || body == null) {
                throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED);
            }
            return body;
        } catch (RestClientException exception) {
            throw new AuthException(AuthErrorCode.GOOGLE_USERINFO_REQUEST_FAILED, exception);
        }
    }
}
