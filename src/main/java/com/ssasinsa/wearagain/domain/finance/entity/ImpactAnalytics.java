package com.ssasinsa.wearagain.domain.finance.entity;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "impact_analytics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImpactAnalytics extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "impact_analytics_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "events_id", nullable = false)
    private Event event;

    @Column(name = "co2_saved", precision = 10, scale = 2)
    private BigDecimal co2Saved;

    @Column(name = "water_saved", precision = 10, scale = 2)
    private BigDecimal waterSaved;

    @Column(name = "energy_saved", precision = 10, scale = 2)
    private BigDecimal energySaved;

    @Builder(access = AccessLevel.PRIVATE)
    private ImpactAnalytics(User user, Event event, BigDecimal co2Saved, BigDecimal waterSaved, BigDecimal energySaved) {
        this.user = user;
        this.event = event;
        this.co2Saved = co2Saved;
        this.waterSaved = waterSaved;
        this.energySaved = energySaved;
    }

    public static ImpactAnalytics create(User user, Event event, BigDecimal co2Saved, BigDecimal waterSaved, BigDecimal energySaved) {
        return ImpactAnalytics.builder()
                .user(user)
                .event(event)
                .co2Saved(co2Saved)
                .waterSaved(waterSaved)
                .energySaved(energySaved)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImpactAnalytics)) {
            return false;
        }
        ImpactAnalytics other = (ImpactAnalytics) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
