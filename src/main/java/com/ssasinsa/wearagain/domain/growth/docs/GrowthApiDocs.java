package com.ssasinsa.wearagain.domain.growth.docs;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class GrowthApiDocs {

    public static final String TAG_NAME = "Growth";
    public static final String TAG_DESCRIPTION = "옷 키우기(Growth) 관련 사용자 API";

    private GrowthApiDocs() {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "Growth 상태 조회",
            description = "마스코트 레벨/경험치 및 환경 임팩트 누적치를 조회합니다."
    )
    public @interface GetStatus {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "랭킹 조회",
            description = "최신 스냅샷(전날 집계) 기준 상위 1~10위와 내 랭킹/순위 변동을 조회합니다."
    )
    public @interface GetRanking {
    }

    @SecurityRequirement(name = "userJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "마법의 가위 사용",
            description = "가위를 사용하여 경험치를 획득하고, 필요 시 보상을 지급합니다."
    )
    public @interface UseMagicScissors {
    }
}
