package com.ssasinsa.wearagain.domain.ranking.controller;

import com.ssasinsa.wearagain.domain.ranking.dto.RankingResponse;
import com.ssasinsa.wearagain.domain.ranking.service.RankingQueryService;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingQueryService rankingQueryService;

    @GetMapping
    public ResponseEntity<RankingResponse> getRanking(@AuthenticationPrincipal AuthenticatedUser user) {
        RankingResponse response = rankingQueryService.getLatestRanking(user.userId());
        return ResponseEntity.ok(response);
    }
}
