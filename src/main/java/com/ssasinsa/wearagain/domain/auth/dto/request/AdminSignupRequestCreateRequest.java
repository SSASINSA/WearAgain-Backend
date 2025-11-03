package com.ssasinsa.wearagain.domain.auth.dto.request;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 가입 신청 요청")
public record AdminSignupRequestCreateRequest(
        @Schema(description = "신청자 이메일", example = "candidate@wearagain.kr")
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @Schema(description = "임시 비밀번호", example = "AdminCandidate1!")
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        @Size(min = 10, message = "비밀번호는 10자 이상이어야 합니다.")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.")
        String password,

        @Schema(description = "신청자 이름", example = "김운영")
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 100, message = "이름은 100자 이하로 입력해 주세요.")
        String name,

        @Schema(description = "신청 역할", example = "ADMIN")
        AdminRole requestedRole,

        @Schema(description = "신청 사유", example = "운영팀 신규 인력 추가 예정")
        @Size(max = 500, message = "신청 사유는 500자 이하로 입력해 주세요.")
        String reason
) {
}
