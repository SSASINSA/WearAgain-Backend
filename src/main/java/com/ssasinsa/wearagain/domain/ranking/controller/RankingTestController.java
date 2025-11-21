package com.ssasinsa.wearagain.domain.ranking.controller;

import com.ssasinsa.wearagain.domain.ranking.dto.RankingAggregateResponse;
import com.ssasinsa.wearagain.domain.ranking.service.RankingAggregationService;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test/ranking")
public class RankingTestController {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final RankingAggregationService rankingAggregationService;

    public RankingTestController(RankingAggregationService rankingAggregationService) {
        this.rankingAggregationService = rankingAggregationService;
    }

    @PostMapping("/aggregate")
    public ResponseEntity<RankingAggregateResponse> aggregateSnapshot(
            @RequestParam(value = "date", required = false) LocalDate snapshotDate
    ) {
        LocalDate target = snapshotDate != null ? snapshotDate : LocalDate.now(ZONE_SEOUL);
        rankingAggregationService.aggregateDaily(target);
        return ResponseEntity.ok(RankingAggregateResponse.ok(target));
    }
}
