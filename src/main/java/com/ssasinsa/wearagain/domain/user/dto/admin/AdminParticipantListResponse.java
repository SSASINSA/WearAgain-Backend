package com.ssasinsa.wearagain.domain.user.dto.admin;

import java.util.List;

public record AdminParticipantListResponse(
        List<AdminParticipantListItemResponse> content,
        long totalElements,
        int totalPages,
        int size,
        int number,
        boolean hasNext,
        boolean hasPrevious,
        AdminParticipantListSummaryResponse summary
) {
    public static AdminParticipantListResponse empty(int size) {
        return new AdminParticipantListResponse(
                List.of(),
                0,
                0,
                size,
                0,
                false,
                false,
                AdminParticipantListSummaryResponse.empty()
        );
    }
}
