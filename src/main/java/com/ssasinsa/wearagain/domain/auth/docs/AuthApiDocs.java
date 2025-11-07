package com.ssasinsa.wearagain.domain.auth.docs;

import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthAuthorizationUrlResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.OAuthLoginResponse;
import com.ssasinsa.wearagain.domain.auth.dto.response.TokenRefreshResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class AuthApiDocs {

    private AuthApiDocs() {
    }

    public static final String TAG_NAME = "User Auth API";
    public static final String TAG_DESCRIPTION = "사용자 OAuth 로그인 및 토큰 발급 API";

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Google OAuth 인증 URL 발급",
            description = "Google 로그인에 사용되는 인가 URL을 생성하여 반환합니다.",
            responseSchema = OAuthAuthorizationUrlResponse.class,
            responseExample = AuthExamples.GOOGLE_AUTHORIZE_RESPONSE
    )
    public @interface GoogleAuthorize {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Google OAuth 로그인",
            description = "Google 인가 코드를 이용하여 Access/Refresh Token을 발급받습니다.",
            requestExample = AuthExamples.OAUTH_LOGIN_REQUEST,
            responseSchema = OAuthLoginResponse.class,
            responseExample = AuthExamples.OAUTH_LOGIN_RESPONSE
    )
    public @interface GoogleCallback {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Kakao OAuth 인증 URL 발급",
            description = "카카오 로그인 시작을 위한 인가 URL을 반환합니다.",
            responseSchema = OAuthAuthorizationUrlResponse.class,
            responseExample = AuthExamples.GOOGLE_AUTHORIZE_RESPONSE
    )
    public @interface KakaoAuthorize {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Kakao OAuth 로그인",
            description = "카카오 인가 코드를 사용하여 웨어어게인 계정을 인증합니다.",
            requestExample = AuthExamples.OAUTH_LOGIN_REQUEST,
            responseSchema = OAuthLoginResponse.class,
            responseExample = AuthExamples.OAUTH_LOGIN_RESPONSE
    )
    public @interface KakaoCallback {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Kakao ID Token 로그인",
            description = "카카오에서 발급한 ID Token을 검증하여 로그인합니다. (모바일 사용)",
            requestExample = """
                    {
                      "idToken": "kakao-id-token"
                    }
                    """,
            responseSchema = OAuthLoginResponse.class,
            responseExample = AuthExamples.OAUTH_LOGIN_RESPONSE
    )
    public @interface KakaoIdToken {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Apple OAuth 로그인",
            description = "Apple 인가 코드와 ID Token을 검증하여 토큰을 발급합니다.",
            requestExample = """
                    {
                      "code": "apple-auth-code",
                      "idToken": "apple-id-token"
                    }
                    """,
            responseSchema = OAuthLoginResponse.class,
            responseExample = AuthExamples.OAUTH_LOGIN_RESPONSE
    )
    public @interface AppleCallback {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "사용자 토큰 재발급",
            description = "Refresh Token을 검증하여 새로운 Access/Refresh Token을 반환합니다.",
            requestExample = AuthExamples.TOKEN_REFRESH_REQUEST,
            responseSchema = TokenRefreshResponse.class,
            responseExample = AuthExamples.TOKEN_REFRESH_RESPONSE
    )
    public @interface TokenRefresh {
    }
}
