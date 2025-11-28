package com.ssasinsa.wearagain.domain.user.dto.admin;

public record AdminMascotResponse(
        int level,
        int exp,
        int nextLevelExp,
        int magicScissorCount,
        int cycles
) {
}
