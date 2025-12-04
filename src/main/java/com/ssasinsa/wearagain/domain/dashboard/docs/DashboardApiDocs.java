package com.ssasinsa.wearagain.domain.dashboard.docs;

import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;

public final class DashboardApiDocs {

    public static final String TAG_NAME = "대시보드";
    public static final String TAG_DESCRIPTION = "대시보드 집계 조회 API";

    private DashboardApiDocs() {
    }

    @ApiDoc(
            summary = "대시보드 지표 조회",
            description = "배치로 집계된 최신 대시보드 스냅샷을 반환합니다.",
            responseSchema = DashboardSnapshotResponse.class
    )
    public @interface GetOverview {
    }
}
