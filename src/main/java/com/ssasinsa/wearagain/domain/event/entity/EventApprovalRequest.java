package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Entity
@Table(name = "event_approval_requests")
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventApprovalRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_approval_request_id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requesting_admin_id", nullable = false)
    private AdminUser requestingAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_admin_id")
    private AdminUser processedByAdmin;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static EventApprovalRequest create(Event event, AdminUser requestingAdmin) {
        return EventApprovalRequest.builder()
                .event(event)
                .requestingAdmin(requestingAdmin)
                .build();
    }

    public void approve(AdminUser approverAdmin, LocalDateTime processedAt) {
        this.processedByAdmin = approverAdmin;
        this.processedAt = processedAt;
        this.event.changeStatus(EventStatus.APPROVAL);
    }

    public void reject(AdminUser rejecterAdmin, LocalDateTime processedAt) {
        this.processedByAdmin = rejecterAdmin;
        this.processedAt = processedAt;
        this.event.changeStatus(EventStatus.REJECTED);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventApprovalRequest)) {
            return false;
        }
        EventApprovalRequest other = (EventApprovalRequest) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
