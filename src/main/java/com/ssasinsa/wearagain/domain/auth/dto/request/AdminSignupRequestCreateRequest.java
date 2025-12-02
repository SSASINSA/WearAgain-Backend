package com.ssasinsa.wearagain.domain.auth.dto.request;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 회원가입 요청 생성 DTO")
public record AdminSignupRequestCreateRequest(
        @Schema(description = "요청자 이메일", example = "candidate@wearagain.kr")
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "올바른 이메일 형식을 입력해 주세요.")
        String email,

        @Schema(description = "임시 비밀번호", example = "AdminCandidate1!")
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.")
        String password,

        @Schema(description = "요청자 이름", example = "담당 관리자")
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 100, message = "이름은 100자 이내로 입력해 주세요.")
        String name,

        @Schema(description = "요청 역할", example = "ADMIN")
        AdminRole requestedRole,

        @Schema(description = "요청 사유", example = "운영 정책 대응을 위한 권한 필요")
        @Size(max = 500, message = "요청 사유는 500자 이내로 입력해 주세요.")
        String reason
) {
}

