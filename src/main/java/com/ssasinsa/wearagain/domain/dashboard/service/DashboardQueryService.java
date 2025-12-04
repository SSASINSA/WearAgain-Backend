package com.ssasinsa.wearagain.domain.dashboard.service;

import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse.ImpactSummary;
import com.ssasinsa.wearagain.domain.dashboard.dto.DashboardSnapshotResponse.OverviewSummary;
import com.ssasinsa.wearagain.domain.dashboard.entity.DashboardSnapshot;
import com.ssasinsa.wearagain.domain.dashboard.repository.DashboardSnapshotRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardQueryService {

    private final DashboardSnapshotRepository dashboardSnapshotRepository;

    @Transactional(readOnly = true)
    public DashboardSnapshotResponse getLatestSnapshot() {
        DashboardSnapshot snapshot = dashboardSnapshotRepository.findTopByOrderByCreatedAtDesc()
                .orElse(null);
        if (snapshot == null) {
            return new DashboardSnapshotResponse(
                    new OverviewSummary(0, 0, 0, 0, 0, null),
                    new ImpactSummary(null, null, null),
                    null
            );
        }
        OverviewSummary overview = new OverviewSummary(
                snapshot.getEventTotalOpenClosed(),
                snapshot.getEventManagerHosted(),
                snapshot.getParticipantsCheckedIn(),      // 누적 참가자 수
                snapshot.getTicketsCharged(),             // 기부된 옷(티켓 충전 수)
                snapshot.getTicketsUsed(),                // 교환된 의류 수
                snapshot.getExchangeRate()
        );
        ImpactSummary impact = new ImpactSummary(
                snapshot.getImpactCo2Saved(),
                snapshot.getImpactWaterSaved(),
                snapshot.getImpactEnergySaved()
        );
        return new DashboardSnapshotResponse(
                overview,
                impact,
                toOffset(snapshot.getCreatedAt())
        );
    }

    private OffsetDateTime toOffset(java.time.LocalDateTime time) {
        return time == null ? null : time.atOffset(ZoneOffset.UTC);
    }
}
