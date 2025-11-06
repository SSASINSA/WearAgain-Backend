package com.ssasinsa.wearagain.domain.event.docs;

import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventAdminListResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventApplicationRejectResponse;
import com.ssasinsa.wearagain.domain.event.dto.admin.EventStaffCodeResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationListResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplicationQrResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCancelResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
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

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 목록 조회",
            description = """
                    관리자 콘솔에서 사용하는 행사 목록 API입니다.
                    상태 필터와 offset 기반 페이지네이션(`page`, `size`)을 지원하며
                    각 행사별 신청 통계(총 좌석/신청 수/잔여 좌석)와 행사 담당 관리자 정보를 함께 제공합니다.
                    """,
            responseSchema = EventAdminListResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_LIST_RESPONSE
    )
    public @interface ListAdminEvents {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 상세 조회",
            description = """
                    관리자 전용 상세 정보(이미지, 옵션 트리, 신청 목록 및 통계)와 행사 담당 관리자 정보를 반환합니다.
                    존재하지 않는 행사 ID 요청 시 404 에러를 반환합니다.
                    """,
            responseSchema = EventAdminDetailResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_DETAIL_RESPONSE
    )
    public @interface GetAdminEventDetail {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 수정",
            description = """
                    행사 기본 정보, 이미지, 옵션 트리를 부분 갱신합니다.
                    `null` 필드는 변경하지 않으며, 빈 배열을 전달하면 해당 목록을 모두 제거합니다.
                    """,
            requestExample = EventExamples.ADMIN_EVENT_UPDATE_REQUEST,
            responseSchema = EventAdminDetailResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_UPDATE_RESPONSE
    )
    public @interface UpdateEvent {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 상태 변경",
            description = """
                    행사 상태를 전환합니다. `SUPER_ADMIN`과 `ADMIN`만 호출할 수 있으며,
                    허용되지 않은 상태 전환(예: `ARCHIVED`에서 `OPEN`) 시 409 에러를 반환합니다.
                    """,
            requestExample = EventExamples.ADMIN_EVENT_STATUS_UPDATE_REQUEST,
            responseSchema = EventAdminDetailResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_STATUS_UPDATE_RESPONSE
    )
    public @interface UpdateEventStatus {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "행사 스태프 코드 발급",
            description = """
                    행사 담당 관리자(organizerAdmin)가 현장 스태프용 6자리 숫자 코드를 발급합니다.
                    발급 시 기존 코드는 즉시 대체되며, 응답에는 새 코드와 발급 시각이 포함됩니다.
                    """,
            responseSchema = EventStaffCodeResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_STAFF_CODE_RESPONSE
    )
    public @interface IssueStaffCode {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "행사 스태프 코드 조회",
            description = """
                    이미 발급된 스태프 코드를 조회합니다.
                    organizerAdmin 본인만 접근할 수 있으며, 아직 코드가 없다면 404를 반환합니다.
                    """,
            responseSchema = EventStaffCodeResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_STAFF_CODE_RESPONSE
    )
    public @interface GetStaffCode {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "관리자 행사 삭제(보관 처리)",
            description = """
                    행사를 물리적으로 삭제하지 않고 `ARCHIVED` 상태로 전환합니다.
                    이미 신청자가 존재하는 경우 409 에러를 반환합니다.
                    """
    )
    public @interface DeleteEvent {
    }

    @SecurityRequirement(name = "adminJWT")
    @ApiDoc(
            summary = "행사 신청 반려",
            description = """
                    특정 신청 건을 반려 처리하고 사유를 기록합니다.
                    이미 반려/취소된 신청은 멱등하게 현재 상태를 반환합니다.
                    """,
            requestExample = EventExamples.ADMIN_EVENT_REJECT_REQUEST,
            responseSchema = EventApplicationRejectResponse.class,
            responseExample = EventExamples.ADMIN_EVENT_REJECT_RESPONSE
    )
    public @interface RejectEventApplication {
    }

    @SecurityRequirement(name = "userJWT")
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

    @SecurityRequirement(name = "userJWT")
    @ApiDoc(
            summary = "사용자 신청 내역 조회",
            description = """
                    사용자의 행사 신청 내역을 커서 기반 페이지네이션으로 조회합니다.
                    상태, 기간(from/to) 필터를 지원하며 응답에 `nextCursor`가 포함됩니다.
                    신청 항목별로 행사 상태(`eventStatus`)와 기간 정보가 함께 제공됩니다.
                    """,
            responseSchema = EventApplicationListResponse.class,
            responseExample = EventExamples.USER_EVENT_APPLICATION_LIST_RESPONSE
    )
    public @interface ListUserApplications {
    }

    @SecurityRequirement(name = "userJWT")
    @ApiDoc(
            summary = "사용자 신청 상세 조회",
            description = """
                    사용자가 자신의 신청 건 상세 정보를 조회합니다.
                    신청자 본인이 아니면 403 에러를 반환하고, 존재하지 않는 신청은 404 에러를 반환합니다.
                    """,
            responseSchema = EventApplicationDetailResponse.class,
            responseExample = EventExamples.USER_EVENT_APPLICATION_DETAIL_RESPONSE
    )
    public @interface GetUserApplicationDetail {
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

    @SecurityRequirement(name = "userJWT")
    @ApiDoc(
            summary = "체크인 QR 토큰 발급",
            description = """
                    사용자가 신청한 행사에 대해 체크인 QR 토큰을 발급하거나 재발급합니다.
                    기존 토큰이 존재하는 경우 Redis에서 제거한 후 새 토큰을 저장합니다.
                    """,
            responseSchema = EventApplicationQrResponse.class,
            responseExample = EventExamples.USER_EVENT_APPLICATION_QR_RESPONSE
    )
    public @interface IssueApplicationQr {
    }
}
