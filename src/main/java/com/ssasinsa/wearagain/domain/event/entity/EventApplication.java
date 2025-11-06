package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "applied_at", updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "checked_in_at"))
})
public class EventApplication extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_applications_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_option_id", nullable = false)
    private EventOption eventOption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventApplicationStatus status;

    @Column(length = 255)
    private String reason;

    @Column(name = "qr_token", length = 64)
    private String qrToken;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private EventApplication(User user, Event event, EventOption eventOption, EventApplicationStatus status, String reason, String qrToken, LocalDateTime canceledAt, LocalDateTime rejectedAt) {
        this.user = user;
        this.event = event;
        this.eventOption = eventOption;
        this.status = status == null ? EventApplicationStatus.APPLIED : status;
        this.reason = reason;
        this.qrToken = qrToken;
        this.canceledAt = canceledAt;
        this.rejectedAt = rejectedAt;
    }

    public static EventApplication create(User user, Event event, EventOption eventOption, EventApplicationStatus status, String reason, String qrToken) {
        EventApplication application = EventApplication.builder()
                .user(user)
                .event(event)
                .eventOption(eventOption)
                .status(status)
                .reason(reason)
                .qrToken(qrToken)
                .build();
        if (event != null) {
            event.addApplication(application);
        }
        if (eventOption != null) {
            eventOption.addApplication(application);
        }
        return application;
    }

    public void cancel(LocalDateTime canceledAt, String reason) {
        this.status = EventApplicationStatus.CANCELED;
        this.canceledAt = canceledAt;
        this.reason = reason;
    }

    public void reject(LocalDateTime rejectedAt, String reason) {
        this.status = EventApplicationStatus.REJECTED;
        this.rejectedAt = rejectedAt;
        this.reason = reason;
    }

    public void checkIn(LocalDateTime checkedInAt) {
        this.status = EventApplicationStatus.CHECKED_IN;
        this.reason = null;
        this.canceledAt = null;
        this.rejectedAt = null;
        this.qrToken = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventApplication)) {
            return false;
        }
        EventApplication other = (EventApplication) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
