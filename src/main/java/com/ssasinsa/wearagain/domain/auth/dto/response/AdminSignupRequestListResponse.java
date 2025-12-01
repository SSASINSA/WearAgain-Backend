package com.ssasinsa.wearagain.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "??? ?? ?? ?? ??")
public record AdminSignupRequestListResponse(
        @Schema(description = "?? ?? ??")
        List<AdminSignupRequestSummaryResponse> items,
        @Schema(description = "?? ??? (0-base)", example = "0")
        int page,
        @Schema(description = "??? ??", example = "20")
        int size,
        @Schema(description = "?? ??", example = "125")
        long totalElements,
        @Schema(description = "?? ??? ?", example = "7")
        int totalPages,
        @Schema(description = "?? ??? ??")
        boolean hasNext
) {

    public static AdminSignupRequestListResponse of(
            List<AdminSignupRequestSummaryResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        return new AdminSignupRequestListResponse(
                List.copyOf(items),
                page,
                size,
                totalElements,
                totalPages,
                hasNext
        );
    }
}
