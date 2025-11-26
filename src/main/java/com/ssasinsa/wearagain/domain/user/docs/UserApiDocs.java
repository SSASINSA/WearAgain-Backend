package com.ssasinsa.wearagain.domain.user.docs;

import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class UserApiDocs {

    public static final String USER_TAG_NAME = "사용자";
    public static final String USER_TAG_DESCRIPTION = "사용자 정보 조회 API";

    private UserApiDocs() {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "사용자 잔여 자원 요약 조회",
            description = "로그인한 사용자의 교환 티켓 및 크레딧 잔여량을 함께 반환합니다.",
            responseSchema = UserSummaryResponse.class,
            responseExample = UserExamples.USER_SUMMARY_RESPONSE
    )
    public @interface GetUserSummary {
    }
}
