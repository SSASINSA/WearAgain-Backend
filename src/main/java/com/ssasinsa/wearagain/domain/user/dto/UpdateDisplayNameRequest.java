package com.ssasinsa.wearagain.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 표시 이름 변경 요청")
public record UpdateDisplayNameRequest(
        @Schema(description = "새 표시 이름", example = "웨어어게인러버")
        @NotBlank(message = "표시 이름을 입력해 주세요.")
        @Size(max = 255, message = "표시 이름은 255자를 넘을 수 없습니다.")
        String displayName
) {
}
