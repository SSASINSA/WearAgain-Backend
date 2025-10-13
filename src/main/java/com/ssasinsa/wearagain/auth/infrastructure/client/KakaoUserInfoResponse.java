package com.ssasinsa.wearagain.auth.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(
        Long id,
        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public String email() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public boolean hasEmail() {
        return kakaoAccount != null && Boolean.TRUE.equals(kakaoAccount.hasEmail());
    }

    public boolean isEmailVerified() {
        return kakaoAccount != null && Boolean.TRUE.equals(kakaoAccount.isEmailVerified());
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    public String profileImageUrl() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }

    public record KakaoAccount(
            String email,
            @JsonProperty("has_email")
            Boolean hasEmail,
            @JsonProperty("is_email_verified")
            Boolean isEmailVerified,
            KakaoProfile profile
    ) {
    }

    public record KakaoProfile(
            String nickname,
            @JsonProperty("profile_image_url")
            String profileImageUrl
    ) {
    }
}
