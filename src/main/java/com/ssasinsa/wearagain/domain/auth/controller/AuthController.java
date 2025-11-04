package com.ssasinsa.wearagain.domain.auth.controller;

import com.ssasinsa.wearagain.domain.auth.docs.AuthApiDocs;
import com.ssasinsa.wearagain.domain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoIdTokenLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.TokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthAuthorizationUrlResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.TokenRefreshResponse;
import com.ssasinsa.wearagain.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = AuthApiDocs.TAG_NAME, description = AuthApiDocs.TAG_DESCRIPTION)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @AuthApiDocs.GoogleAuthorize
    @GetMapping("/google/authorize-url")
    public ResponseEntity<OAuthAuthorizationUrlResponse> getGoogleAuthorizationUrl() {
        String authorizationUrl = authService.generateGoogleAuthorizationUrl();
        return ResponseEntity.ok(new OAuthAuthorizationUrlResponse(authorizationUrl));
    }

    @AuthApiDocs.GoogleCallback
    @PostMapping("/google/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithGoogle(@Valid @RequestBody GoogleOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @AuthApiDocs.KakaoAuthorize
    @GetMapping("/kakao/authorize-url")
    public ResponseEntity<OAuthAuthorizationUrlResponse> getKakaoAuthorizationUrl() {
        String authorizationUrl = authService.generateKakaoAuthorizationUrl();
        return ResponseEntity.ok(new OAuthAuthorizationUrlResponse(authorizationUrl));
    }

    @AuthApiDocs.KakaoCallback
    @PostMapping("/kakao/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithKakao(@Valid @RequestBody KakaoOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithKakao(request);
        return ResponseEntity.ok(response);
    }

    @AuthApiDocs.KakaoIdToken
    @PostMapping("/kakao/id-token")
    public ResponseEntity<OAuthLoginResponse> loginWithKakaoIdToken(@Valid @RequestBody KakaoIdTokenLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithKakaoIdToken(request);
        return ResponseEntity.ok(response);
    }

    @AuthApiDocs.AppleCallback
    @PostMapping("/apple/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithApple(@Valid @RequestBody AppleOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithApple(request);
        return ResponseEntity.ok(response);
    }

    @AuthApiDocs.TokenRefresh
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
