package com.ssasinsa.wearagain.domain.user.dto.admin;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 계정 목록 응답")
public record AdminManagedUserListResponse(
        @Schema(description = "관리자 계정 목록")
        List<AdminManagedUserResponse> content,
        @Schema(description = "현재 페이지 번호", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 요소 수", example = "6")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "1")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "false")
        boolean hasNext
) {

    public static AdminManagedUserListResponse of(
            List<AdminManagedUserResponse> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new AdminManagedUserListResponse(content, page, size, totalElements, totalPages, hasNext);
    }
}
