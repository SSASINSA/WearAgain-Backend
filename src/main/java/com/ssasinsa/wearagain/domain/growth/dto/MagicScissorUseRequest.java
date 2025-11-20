package com.ssasinsa.wearagain.domain.growth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MagicScissorUseRequest(
        @Min(1)
        @Max(20)
        int useCount
) {
}
