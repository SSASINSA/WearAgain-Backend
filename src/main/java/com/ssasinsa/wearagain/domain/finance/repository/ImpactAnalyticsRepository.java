package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.ImpactAnalytics;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImpactAnalyticsRepository extends JpaRepository<ImpactAnalytics, Long> {

    @Query("SELECT new com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary("
            + "COALESCE(SUM(ia.co2Saved), 0), "
            + "COALESCE(SUM(ia.waterSaved), 0), "
            + "COALESCE(SUM(ia.energySaved), 0)) "
            + "FROM ImpactAnalytics ia WHERE ia.user.id = :userId")
    ImpactSummary aggregateByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    @Query("SELECT new com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary("
            + "COALESCE(SUM(ia.co2Saved), 0), "
            + "COALESCE(SUM(ia.waterSaved), 0), "
            + "COALESCE(SUM(ia.energySaved), 0)) "
            + "FROM ImpactAnalytics ia")
    ImpactSummary aggregateTotalImpact();

    @Query("SELECT new com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary("
            + "COALESCE(SUM(ia.co2Saved), 0), "
            + "COALESCE(SUM(ia.waterSaved), 0), "
            + "COALESCE(SUM(ia.energySaved), 0)) "
            + "FROM ImpactAnalytics ia WHERE ia.event.id = :eventId")
    ImpactSummary aggregateByEventId(@Param("eventId") Long eventId);
}
