package com.ssasinsa.wearagain.domain.growth.dto;

public record GrowthStatusResponse(
        MascotResponse mascot,
        ImpactSummary impact
) {

    public static GrowthStatusResponse of(MascotStatusDto statusDto) {
        MascotResponse mascotResponse = new MascotResponse(
                statusDto.level(),
                statusDto.exp(),
                statusDto.nextLevelExp(),
                statusDto.magicScissorCount(),
                statusDto.cycles()
        );
        return new GrowthStatusResponse(mascotResponse, statusDto.impact());
    }

    public record MascotResponse(
            int level,
            int exp,
            int nextLevelExp,
            int magicScissorCount,
            int cycles
    ) {}
}
