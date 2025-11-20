package com.ssasinsa.wearagain.domain.growth.docs;

import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;

public final class GrowthApiDocs {

    public static final String TAG_NAME = "Growth";
    public static final String TAG_DESCRIPTION = "옷 키우기(Growth) 관련 사용자 API";

    private GrowthApiDocs() {
    }

    @ApiDoc(
            summary = "Growth 상태 조회",
            description = "마스코트 레벨/경험치 및 환경 임팩트 누적치를 조회합니다."
    )
    public @interface GetStatus {
    }

    @ApiDoc(
            summary = "마법의 가위 사용",
            description = "가위를 사용하여 경험치를 획득하고, 필요 시 보상을 지급합니다."
    )
    public @interface UseMagicScissors {
    }
}
