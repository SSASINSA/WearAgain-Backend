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
    public static final String TAG_DESCRIPTION = "관리자 참가자 현황/제어 API";

    private AdminParticipantApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 목록 조회",
            description = """
                    suspended(정지 여부), sortBy(정렬 기준), keyword/keywordScope(검색 범위), page/size를 조합해 참가자 목록을 조회합니다.
                    keywordScope 는 `ALL|EMAIL|NAME` 중 하나를 전달하며, 값이 비어 있으면 ALL 로 처리됩니다.
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
            description = "선택한 참가자의 프로필, 임팩트 지표, 성장/마스코트 정보, 최근 신청 이벤트를 모두 제공합니다.",
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
            description = "전체 참가자 수와 누적 티켓/크레딧 보유 현황을 조회하여 대시보드에 노출합니다.",
            responseSchema = AdminParticipantStatsResponse.class,
            responseExample = AdminParticipantExamples.PARTICIPANT_STATS_RESPONSE
    )
    public @interface GetParticipantStats {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 티켓/크레딧 잔액 조정",
            description = """
                    ticketBalance 와 creditBalance 중 전달된 필드만 갱신합니다. 각 값은 0 이상의 정수여야 하며,
                    null 로 전달되면 기존 값을 유지합니다. 정지/해제는 별도의 `/suspension` API 를 사용해야 합니다.
                    """,
            requestExample = AdminParticipantExamples.PARTICIPANT_UPDATE_REQUEST,
            responseSchema = AdminParticipantDetailResponse.class,
            responseExample = AdminParticipantExamples.PARTICIPANT_UPDATE_RESPONSE
    )
    public @interface UpdateParticipantBalance {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "참가자 정지/해제",
            description = """
                    suspended 값을 true/false 로 전달하여 참가자 계정을 정지 또는 해제합니다.
                    정지 시 Redis 에 저장된 Refresh Token 을 삭제해 모든 기기에서 로그아웃됩니다.
                    """,
            requestExample = AdminParticipantExamples.SUSPENSION_REQUEST,
            responseSchema = AdminParticipantDetailResponse.class,
            responseExample = AdminParticipantExamples.SUSPENSION_RESPONSE
    )
    public @interface UpdateSuspension {
    }
}
