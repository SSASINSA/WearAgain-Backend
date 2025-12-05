package com.ssasinsa.wearagain.domain.user.docs;

import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class AdminParticipantApiDocs {

    public static final String TAG_NAME = "Admin Participant";
    public static final String TAG_DESCRIPTION = "관리자 참가자 현황/제재 관리 API";

    private AdminParticipantApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 목록 조회",
            description = """
                    suspended(정지 여부), sortBy(정렬 기준), keyword/keywordScope(검색 범위)와 page/size를 조합해 참가자 목록을 조회합니다.
                    keywordScope는 `ALL|EMAIL|NAME` 중 하나로 전달하며 값이 비어있거나 유효하지 않으면 ALL로 처리됩니다.
                    """,
            responseSchema = AdminParticipantListResponse.class,
            responseExample = AdminParticipantExamples.PARTICIPANT_LIST_RESPONSE
    )
    public @interface GetParticipants {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 상세 조회",
            description = "선택된 참가자의 프로필, 임팩트 지표, 레벨/마스코트 정보, 최근 행사 참여 이력을 함께 제공합니다.",
            responseSchema = AdminParticipantDetailResponse.class,
            responseExample = AdminParticipantExamples.PARTICIPANT_DETAIL_RESPONSE
    )
    public @interface GetParticipantDetail {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 통계 조회",
            description = "전체 참가자 수와 누적 티켓/크레딧 보유량을 조회해 대시보드에 노출합니다.",
            responseSchema = AdminParticipantStatsResponse.class,
            responseExample = AdminParticipantExamples.PARTICIPANT_STATS_RESPONSE
    )
    public @interface GetParticipantStats {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 정지/해제",
            description = """
                    suspended 값을 true/false로 전달해 참가자를 정지하거나 해제합니다.
                    정지 시에는 Redis에 저장된 Refresh Token을 비워 모든 세션을 강제 만료시킵니다.
                    """,
            requestExample = AdminParticipantExamples.SUSPENSION_REQUEST,
            responseSchema = AdminParticipantDetailResponse.class,
            responseExample = AdminParticipantExamples.SUSPENSION_RESPONSE
    )
    public @interface UpdateSuspension {
    }
}
