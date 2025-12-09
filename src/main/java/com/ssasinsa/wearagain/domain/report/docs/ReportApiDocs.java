package com.ssasinsa.wearagain.domain.report.docs;

import com.ssasinsa.wearagain.domain.report.dto.ReportCreateResponse;
import com.ssasinsa.wearagain.domain.report.dto.ReportStatusResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ReportApiDocs {

    private ReportApiDocs() {
    }

    public static final String TAG_NAME = "Event Report API";
    public static final String TAG_DESCRIPTION = "행사 종료 후 지표를 집계하여 PDF 리포트를 생성/조회하는 API";

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "행사 리포트 생성 요청",
            description = """
                    종료된 행사 ID를 전달하면 리포트 생성 작업을 비동기로 등록합니다.
                    응답은 reportId와 생성 상태(PENDING)를 반환하며, READY 상태가 되면 다운로드 링크가 제공됩니다.
                    """
    )
    public @interface CreateEventReport {
    }

    @SecurityRequirement(name = "adminJWT")
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @ApiDoc(
            summary = "리포트 생성 상태 조회",
            description = """
                    reportId로 생성 상태를 조회합니다.
                    READY 상태일 때만 downloadUrl이 포함되며, FAILED 상태에서는 message가 설정됩니다.
                    """,
            responseSchema = ReportStatusResponse.class
    )
    public @interface GetReportStatus {
    }
}
