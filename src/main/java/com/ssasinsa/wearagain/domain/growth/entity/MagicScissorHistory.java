package com.ssasinsa.wearagain.domain.growth.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "magic_scissor_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MagicScissorHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "magic_scissor_histories_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_growths_id", nullable = false)
    private UserGrowth userGrowth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_events_id")
    private Event relatedEvent;

    @Column(nullable = false)
    private int delta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MagicScissorHistoryReason reason;

    @Column(length = 255)
    private String memo;

    @Builder(access = AccessLevel.PRIVATE)
    private MagicScissorHistory(User user, UserGrowth userGrowth, Event relatedEvent, int delta,
            MagicScissorHistoryReason reason, String memo) {
        this.user = user;
        this.userGrowth = userGrowth;
        this.relatedEvent = relatedEvent;
        this.delta = delta;
        this.reason = reason;
        this.memo = memo;
    }

    public static MagicScissorHistory create(User user, UserGrowth userGrowth, Event relatedEvent, int delta,
            MagicScissorHistoryReason reason, String memo) {
        return MagicScissorHistory.builder()
                .user(user)
                .userGrowth(userGrowth)
                .relatedEvent(relatedEvent)
                .delta(delta)
                .reason(reason)
                .memo(memo)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MagicScissorHistory)) {
            return false;
        }
        MagicScissorHistory other = (MagicScissorHistory) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
