package com.ssasinsa.wearagain.domain.dashboard.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dashboard_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class DashboardSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dashboard_snapshot_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "event_total_open_closed", nullable = false)
    private long eventTotalOpenClosed;

    @Column(name = "event_manager_hosted", nullable = false)
    private long eventManagerHosted;

    @Column(name = "participants_checked_in", nullable = false)
    private long participantsCheckedIn;

    @Column(name = "tickets_charged", nullable = false)
    private long ticketsCharged;

    @Column(name = "tickets_used", nullable = false)
    private long ticketsUsed;

    @Column(name = "exchange_rate", nullable = false, precision = 19, scale = 4)
    private BigDecimal exchangeRate;

    @Column(name = "impact_co2_saved", nullable = false, precision = 19, scale = 4)
    private BigDecimal impactCo2Saved;

    @Column(name = "impact_water_saved", nullable = false, precision = 19, scale = 4)
    private BigDecimal impactWaterSaved;

    @Column(name = "impact_energy_saved", nullable = false, precision = 19, scale = 4)
    private BigDecimal impactEnergySaved;

    public static DashboardSnapshot create(
            long eventTotalOpenClosed,
            long eventManagerHosted,
            long participantsCheckedIn,
            long ticketsCharged,
            long ticketsUsed,
            BigDecimal exchangeRate,
            BigDecimal impactCo2Saved,
            BigDecimal impactWaterSaved,
            BigDecimal impactEnergySaved
    ) {
        return DashboardSnapshot.builder()
                .eventTotalOpenClosed(eventTotalOpenClosed)
                .eventManagerHosted(eventManagerHosted)
                .participantsCheckedIn(participantsCheckedIn)
                .ticketsCharged(ticketsCharged)
                .ticketsUsed(ticketsUsed)
                .exchangeRate(exchangeRate)
                .impactCo2Saved(impactCo2Saved)
                .impactWaterSaved(impactWaterSaved)
                .impactEnergySaved(impactEnergySaved)
                .build();
    }
}
