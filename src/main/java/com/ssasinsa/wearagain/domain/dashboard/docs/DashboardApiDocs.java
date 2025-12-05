package com.ssasinsa.wearagain.domain.dashboard.docs;

import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.EventMetricsResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class DashboardApiDocs {

    public static final String TAG_NAME = "대시보드";
    public static final String TAG_DESCRIPTION = "대시보드 집계 조회 API";

    private DashboardApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "대시보드 지표 조회",
            description = "배치로 집계된 최신 대시보드 스냅샷을 반환합니다.",
            responseSchema = DashboardSnapshotResponse.class
    )
    public @interface GetOverview {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "기간별 이벤트 지표 조회",
            description = "period(MONTH_1, MONTH_3, YEAR_1, 기본값 MONTH_1) 쿼리 파라미터를 기준으로 기간 내 진행된 이벤트별 참가자 수, 기부된 옷(티켓 충전), 교환된 옷(티켓 사용)을 조회합니다. 응답은 {count, items[]} 형태로 반환됩니다.",
            responseSchema = EventMetricsResponse.class
    )
    public @interface GetEventMetrics {
    }
}
