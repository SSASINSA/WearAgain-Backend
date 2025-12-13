package com.ssasinsa.wearagain.domain.user.dto.admin;

import java.math.BigDecimal;

public record AdminMascotResponse(
        int level,
        int exp,
        int nextLevelExp,
        int expRemainingToNextLevel,
        BigDecimal expProgressPercent,
        int magicScissorCount,
        int cycles
) {
}
