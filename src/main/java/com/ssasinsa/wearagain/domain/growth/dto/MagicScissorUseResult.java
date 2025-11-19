package com.ssasinsa.wearagain.domain.growth.dto;

public record MagicScissorUseResult(
        int level,
        int exp,
        int magicScissorCount,
        int cycles,
        boolean rewardGranted,
        int rewardCredit
) {
}
