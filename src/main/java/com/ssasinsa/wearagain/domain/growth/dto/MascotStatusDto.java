package com.ssasinsa.wearagain.domain.growth.dto;

import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;

public record MascotStatusDto(
        int level,
        int exp,
        int nextLevelExp,
        int magicScissorCount,
        int cycles,
        ImpactSummary impact
) {
    public static MascotStatusDto of(UserGrowth userGrowth, int nextLevelExp, ImpactSummary impactSummary) {
        return new MascotStatusDto(
                userGrowth.getCurrentLevel(),
                userGrowth.getExp(),
                nextLevelExp,
                userGrowth.getMagicScissorCount(),
                userGrowth.getCycles(),
                impactSummary
        );
    }
}
