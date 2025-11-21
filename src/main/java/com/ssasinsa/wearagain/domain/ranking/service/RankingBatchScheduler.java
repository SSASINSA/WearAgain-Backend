package com.ssasinsa.wearagain.domain.ranking.service;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchScheduler {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final RankingAggregationService rankingAggregationService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void aggregateDailyRanking() {
        rankingAggregationService.aggregateDaily(LocalDate.now(ZONE_SEOUL));
    }
}
