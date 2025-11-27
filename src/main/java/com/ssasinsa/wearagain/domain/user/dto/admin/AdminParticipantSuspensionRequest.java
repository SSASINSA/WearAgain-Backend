package com.ssasinsa.wearagain.domain.user.dto.admin;

import jakarta.validation.constraints.NotNull;

public record AdminParticipantSuspensionRequest(@NotNull Boolean suspended) {
}
