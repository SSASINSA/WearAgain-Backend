package com.ssasinsa.wearagain.domain.event.docs;

import com.ssasinsa.wearagain.domain.event.dto.manager.EventMetricResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class EventMetricsApiDocs {

    public static final String TAG_NAME = "이벤트 지표(그래프)";
    public static final String TAG_DESCRIPTION = "기간별 이벤트 지표 조회 API";

    private EventMetricsApiDocs() {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "기간별 이벤트 지표 조회",
            description = "period(MONTH_1, MONTH_3, YEAR_1, 기본값 MONTH_1) 쿼리 파라미터를 기준으로 기간 내 진행된 이벤트별 참가자 수, 기부된 옷(티켓 충전), 교환된 옷(티켓 사용)을 조회합니다.",
            responseSchema = EventMetricResponse.class
    )
    public @interface GetEventMetrics {
    }
}
