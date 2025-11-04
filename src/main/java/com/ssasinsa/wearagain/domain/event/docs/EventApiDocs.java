package com.ssasinsa.wearagain.domain.event.docs;

import com.ssasinsa.wearagain.domain.event.dto.response.EventCreateResponse;
import com.ssasinsa.wearagain.domain.event.dto.response.EventImageUploadResponse;
import com.ssasinsa.wearagain.global.docs.annotation.ApiDoc;

public final class EventApiDocs {

    private EventApiDocs() {
    }

    public static final String TAG_NAME = "Event Admin API";
    public static final String TAG_DESCRIPTION = "관리자 행사 등록 및 이미지 업로드 API";

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
}
