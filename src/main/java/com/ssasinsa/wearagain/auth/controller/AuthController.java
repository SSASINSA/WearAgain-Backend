package com.ssasinsa.wearagain.auth.controller;

import com.ssasinsa.wearagain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.auth.dto.response.OAuthAuthorizationUrlResponse;
import com.ssasinsa.wearagain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google/authorize-url")
    public ResponseEntity<OAuthAuthorizationUrlResponse> getGoogleAuthorizationUrl() {
        String authorizationUrl = authService.generateGoogleAuthorizationUrl();
        return ResponseEntity.ok(new OAuthAuthorizationUrlResponse(authorizationUrl));
    }

    @PostMapping("/google/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithGoogle(@Valid @RequestBody GoogleOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithGoogle(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kakao/authorize-url")
    public ResponseEntity<OAuthAuthorizationUrlResponse> getKakaoAuthorizationUrl() {
        String authorizationUrl = authService.generateKakaoAuthorizationUrl();
        return ResponseEntity.ok(new OAuthAuthorizationUrlResponse(authorizationUrl));
    }

    @PostMapping("/kakao/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithKakao(@Valid @RequestBody KakaoOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithKakao(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/apple/callback")
    public ResponseEntity<OAuthLoginResponse> loginWithApple(@Valid @RequestBody AppleOAuthLoginRequest request) {
        OAuthLoginResponse response = authService.loginWithApple(request);
        return ResponseEntity.ok(response);
    }
}
