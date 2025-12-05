package com.ssasinsa.wearagain.domain.dashboard.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DashboardSnapshotResponse(
        OverviewSummary overview,
        ImpactSummary impact,
        OffsetDateTime capturedAt
) {

    public record OverviewSummary(
            long totalOpenOrClosed,
            long managerHosted,
            long cumulativeParticipants,
            long donatedClothes,
            long exchangedClothes,
            BigDecimal exchangeRate
    ) {}

    public record ImpactSummary(BigDecimal co2Saved, BigDecimal waterSaved, BigDecimal energySaved) {}
}
