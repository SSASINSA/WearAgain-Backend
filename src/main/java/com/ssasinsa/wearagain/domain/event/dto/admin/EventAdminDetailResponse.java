package com.ssasinsa.wearagain.domain.event.dto.admin;

import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "관리자용 행사 상세 응답")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventAdminDetailResponse {

    @Schema(description = "행사 ID", example = "101")
    private Long eventId;

    @Schema(description = "행사 제목", example = "지속가능 패션 워크숍")
    private String title;

    @Schema(description = "행사 상세 설명", example = "웨어어게인과 함께하는 리폼 클래스")
    private String description;

    @Schema(description = "이용 방법 안내", example = "준비물은 개인 텀블러를 지참해주세요.")
    private String usageGuide;

    @Schema(description = "주의 사항", example = "화재 예방을 위해 지정된 구역에서만 작업해주세요.")
    private String precautions;

    @Schema(description = "행사 위치", example = "서울시 마포구 연남동 223-14 2F")
    private String location;

    @Schema(description = "주최자 이름", example = "운영자")
    private String organizerName;

    @Schema(description = "주최자 이메일", example = "admin@wearagain.kr")
    private String organizerContact;

    @Schema(description = "행사 담당 관리자 ID", example = "11")
    private Long organizerAdminId;

    @Schema(description = "행사 담당 관리자 이메일", example = "admin@wearagain.kr")
    private String organizerAdminEmail;

    @Schema(description = "행사 담당 관리자 이름", example = "홍길동")
    private String organizerAdminName;

    @Schema(description = "행사 시작일", example = "2025-11-10")
    private LocalDate startDate;

    @Schema(description = "행사 종료일", example = "2025-11-30")
    private LocalDate endDate;

    @Schema(description = "행사 상태", example = "OPEN")
    private EventStatus status;

    @Schema(description = "총 수용 인원", example = "120")
    private Long totalCapacity;

    @Schema(description = "현재 신청 인원", example = "87")
    private Long appliedCount;

    @Schema(description = "잔여 인원", example = "33")
    private Long remainingCount;

    @Schema(description = "스태프 코드", example = "023941")
    private String staffCode;

    @Schema(description = "스태프 코드 발급 시각(UTC)", example = "2025-02-01T10:15:20Z")
    private OffsetDateTime staffCodeIssuedAt;

    @Schema(description = "생성 시각(UTC)", example = "2025-10-21T11:20:05Z")
    private OffsetDateTime createdAt;

    @Schema(description = "수정 시각(UTC)", example = "2025-11-01T09:00:00Z")
    private OffsetDateTime updatedAt;

    @Schema(description = "이미지 목록")
    private List<EventAdminImageResponse> images;

    @Schema(description = "옵션 트리")
    private List<EventAdminOptionResponse> options;

    @Schema(description = "신청 목록")
    private List<EventAdminApplicationResponse> applications;

    @Schema(description = "행사 이미지 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventAdminImageResponse {
        @Schema(description = "이미지 ID", example = "1001")
        private Long imageId;

        @Schema(description = "이미지 URL", example = "https://cdn.wearagain.kr/events/123/main.jpg")
        private String url;

        @Schema(description = "대체 텍스트", example = "행사 대표 이미지")
        private String altText;

        @Schema(description = "정렬 순서 (1부터 시작)", example = "1")
        private int displayOrder;
    }

    @Schema(description = "행사 옵션 정보(신청 현황 포함)")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventAdminOptionResponse {
        @Schema(description = "옵션 ID", example = "2001")
        private Long optionId;

        @Schema(description = "옵션 이름", example = "11월 15일")
        private String name;

        @Schema(description = "옵션 타입 라벨", example = "DATE")
        private String type;

        @Schema(description = "정렬 순서", example = "1")
        private int displayOrder;

        @Schema(description = "수용 인원(없으면 null)", example = "30")
        private Integer capacity;

        @Schema(description = "현재 신청 인원", example = "25")
        private Integer appliedCount;

        @Schema(description = "잔여 인원", example = "5")
        private Integer remainingCount;

        @Schema(description = "하위 옵션 목록")
        private List<EventAdminOptionResponse> children;
    }

    @Schema(description = "행사 신청 요약 정보")
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventAdminApplicationResponse {
        @Schema(description = "신청 ID", example = "5001")
        private Long applicationId;

        @Schema(description = "신청자 이메일", example = "user@wearagain.kr")
        private String email;

        @Schema(description = "신청자 이름", example = "사용자")
        private String displayName;

        @Schema(description = "연결된 옵션 ID", example = "2003")
        private Long optionId;

        @Schema(description = "신청 상태", example = "APPLIED")
        private String status;

        @Schema(description = "신청 시각(UTC)", example = "2025-11-12T04:00:00Z")
        private OffsetDateTime appliedAt;

        @Schema(description = "반려/취소 사유", example = "예약 인원 초과")
        private String reason;
    }
}

