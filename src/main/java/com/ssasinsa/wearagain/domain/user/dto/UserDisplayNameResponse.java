package com.ssasinsa.wearagain.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 표시 이름 응답")
public record UserDisplayNameResponse(
        @Schema(description = "변경된 표시 이름", example = "웨어어게인러버")
        String displayName
) {
}
