package com.ssasinsa.wearagain.domain.auth.service;

import com.ssasinsa.wearagain.domain.auth.dto.request.AppleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.GoogleOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoIdTokenLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.KakaoOAuthLoginRequest;
import com.ssasinsa.wearagain.domain.auth.dto.request.TokenRefreshRequest;
import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.TokenRefreshResponse;

public interface AuthService {

    OAuthLoginResponse loginWithGoogle(GoogleOAuthLoginRequest request);

    OAuthLoginResponse loginWithKakao(KakaoOAuthLoginRequest request);

    OAuthLoginResponse loginWithKakaoIdToken(KakaoIdTokenLoginRequest request);

    OAuthLoginResponse loginWithApple(AppleOAuthLoginRequest request);

    TokenRefreshResponse refreshToken(TokenRefreshRequest request);

    String generateGoogleAuthorizationUrl();

    String generateKakaoAuthorizationUrl();
}
