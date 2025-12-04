package com.ssasinsa.wearagain.domain.dashboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardSnapshotScheduler {

    private final DashboardAggregationService dashboardAggregationService;

    @EventListener(ApplicationReadyEvent.class)
    public void aggregateOnStartup() {
        aggregateSnapshot();
    }

    // 트래픽이 적은 시간대에 집계 (예: 02:30 KST)
    @Scheduled(cron = "0 30 2 * * *", zone = "Asia/Seoul")
    public void aggregateNightly() {
        aggregateSnapshot();
    }

    private void aggregateSnapshot() {
        var snapshot = dashboardAggregationService.aggregate();
        log.info("[DashboardSnapshot] captured snapshot id={} at {}", snapshot.getId(), snapshot.getCreatedAt());
    }
}
