package com.ssasinsa.wearagain.domain.auth.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "admin_signup_requests")
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminSignupRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_signup_requests_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole requestedRole;

    @Column(length = 500)
    private String reason;

    @Column(length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminSignupRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private AdminUser reviewedBy;

    @Column
    private LocalDateTime reviewedAt;

    public static AdminSignupRequest createPending(String email, String encodedPassword, String name, AdminRole requestedRole, String reason) {
        return AdminSignupRequest.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .requestedRole(requestedRole == null ? AdminRole.MANAGER : requestedRole)
                .reason(reason)
                .status(AdminSignupRequestStatus.PENDING)
                .build();
    }

    public boolean isPending() {
        return status == AdminSignupRequestStatus.PENDING;
    }

    public boolean isExpired(LocalDateTime now, long expiryHours) {
        return isPending() && getCreatedAt() != null && getCreatedAt().plusHours(expiryHours).isBefore(now);
    }

    public void markApproved(AdminUser reviewer, LocalDateTime reviewedAt) {
        this.status = AdminSignupRequestStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
    }

    public void markRejected(AdminUser reviewer, LocalDateTime reviewedAt, String rejectionReason) {
        this.status = AdminSignupRequestStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewedAt = reviewedAt;
        this.rejectionReason = rejectionReason;
    }

    public void markExpired() {
        this.status = AdminSignupRequestStatus.EXPIRED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdminSignupRequest)) {
            return false;
        }
        AdminSignupRequest other = (AdminSignupRequest) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
