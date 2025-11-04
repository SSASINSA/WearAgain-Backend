package com.ssasinsa.wearagain.domain.event.docs;

import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventImageUploadResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventListResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public final class EventApiDocs {

    private EventApiDocs() {
    }

    public static final String TAG_NAME = "Event Admin API";
    public static final String TAG_DESCRIPTION = "관리자 행사 등록 및 이미지 업로드 API";
    public static final String USER_TAG_NAME = "Event User API";
    public static final String USER_TAG_DESCRIPTION = "사용자용 행사 조회 및 신청 API";

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 등록",
            description = """
                    관리자 백오피스에서 행사 기본 정보, 이미지 배열, 옵션 트리를 등록합니다.
                    이미지 URL과 옵션 구조는 사전에 검증되며, 저장 결과로 생성된 ID와 구조를 반환합니다.
                    """,
            requestExample = EventExamples.ADMIN_EVENT_CREATE_REQUEST,
            responseSchema = EventCreateResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_CREATE_RESPONSE
    )
    public @interface CreateEvent {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "행사 이미지 업로드",
            description = """
                    멀티파트 이미지를 업로드하여 `/data/uploads` 경로에 저장하고
                    추후 행사 생성 요청에 사용할 imageName/imageUrl 정보를 반환합니다.
                    """,
            responseSchema = EventImageUploadResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_IMAGE_UPLOAD_RESPONSE
    )
    public @interface UploadImage {
    }

    @ApiDoc(
            summary = "사용자 행사 목록 조회",
            description = """
                    공개된 행사를 상태별로 조회합니다.
                    기본값은 OPEN 상태이며 cursor 기반 페이지네이션(`cursor`, `size`)을 지원합니다.
                    응답의 `nextCursor` 값을 다음 호출의 cursor로 전달하면 이후 데이터를 조회할 수 있습니다.
                    """,
            responseSchema = EventListResponse.class,
            responseExample = EventExamples.USER_EVENT_LIST_RESPONSE
    )
    public @interface ListEvents {
    }

    @ApiDoc(
            summary = "사용자 행사 상세 조회",
            description = """
                    단일 행사의 상세 정보와 이미지, 옵션 트리를 조회합니다.
                    DRAFT/ARCHIVED 상태의 행사는 노출되지 않습니다.
                    """,
            responseSchema = EventDetailResponse.class,
            responseExample = EventExamples.USER_EVENT_DETAIL_RESPONSE
    )
    public @interface GetEventDetail {
    }

    @SecurityRequirement(name = "userJWT")
    @ApiDoc(
            summary = "행사 신청",
            description = """
                    사용자가 행사 옵션을 선택해 신청합니다.
                    신청 중/입장 완료 상태의 중복 신청을 방지하고, 옵션 수용 인원을 검증합니다.
                    """,
            requestExample = EventExamples.USER_EVENT_APPLY_REQUEST,
            responseSchema = EventApplyResponse.class,
            responseExample = EventExamples.USER_EVENT_APPLY_RESPONSE
    )
    public @interface ApplyEvent {
    }

    @SecurityRequirement(name = "userJWT")
    @ApiDoc(
            summary = "행사 신청 취소",
            description = """
                    사용자가 자신의 신청 건을 취소합니다.
                    이미 취소/반려/입장 처리된 신청은 취소할 수 없습니다.
                    """,
            requestExample = EventExamples.USER_EVENT_CANCEL_REQUEST,
            responseSchema = EventCancelResponse.class,
            responseExample = EventExamples.USER_EVENT_CANCEL_RESPONSE
    )
    public @interface CancelEvent {
    }
}
