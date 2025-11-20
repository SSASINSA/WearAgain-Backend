package com.ssasinsa.wearagain.domain.growth.dto;

public record MagicScissorUseResponse(
        GrowthStatusResponse.MascotResponse mascot,
        RewardResponse reward
) {

    public static MagicScissorUseResponse from(MagicScissorUseResult result, int nextLevelExp) {
        GrowthStatusResponse.MascotResponse mascot = new GrowthStatusResponse.MascotResponse(
                result.level(),
                result.exp(),
                nextLevelExp,
                result.magicScissorCount(),
                result.cycles()
        );
        RewardResponse reward = new RewardResponse(result.rewardGranted(), result.rewardCredit());
        return new MagicScissorUseResponse(mascot, reward);
    }

    public record RewardResponse(
            boolean rewardGranted,
            int credit
    ) {}
}
