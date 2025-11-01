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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "ticket_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_histories_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_events_id")
    private Event relatedEvent;

    @Column(name = "change_amount", nullable = false)
    private int changeAmount;

    @Column(length = 255)
    private String reason;

    @Builder(access = AccessLevel.PRIVATE)
    private TicketHistory(User user, Event relatedEvent, int changeAmount, String reason) {
        this.user = user;
        this.relatedEvent = relatedEvent;
        this.changeAmount = changeAmount;
        this.reason = reason;
    }

    public static TicketHistory create(User user, Event relatedEvent, int changeAmount, String reason) {
        return TicketHistory.builder()
                .user(user)
                .relatedEvent(relatedEvent)
                .changeAmount(changeAmount)
                .reason(reason)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TicketHistory)) {
            return false;
        }
        TicketHistory other = (TicketHistory) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
