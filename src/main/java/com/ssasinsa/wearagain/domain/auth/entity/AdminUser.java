package com.ssasinsa.wearagain.domain.auth.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "admin_users")
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_users_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminStatus status;

    @Column(nullable = false)
    private boolean mustChangePassword;

    @Column
    private LocalDateTime lastLoginAt;

    public static AdminUser createSuperAdmin(String email, String encodedPassword, String name) {
        return AdminUser.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .role(AdminRole.SUPER_ADMIN)
                .status(AdminStatus.ACTIVE)
                .mustChangePassword(false)
                .lastLoginAt(null)
                .build();
    }

    public static AdminUser createApproved(String email, String encodedPassword, String name, AdminRole role) {
        AdminRole resolvedRole = role == null ? AdminRole.MANAGER : role;
        if (resolvedRole == AdminRole.SUPER_ADMIN) {
            resolvedRole = AdminRole.ADMIN;
        }
        return AdminUser.builder()
                .email(email)
                .password(encodedPassword)
                .name(name)
                .role(resolvedRole)
                .status(AdminStatus.ACTIVE)
                .mustChangePassword(true)
                .lastLoginAt(null)
                .build();
    }

    public void changePassword(String encodedPassword, boolean requireChange) {
        this.password = encodedPassword;
        this.mustChangePassword = requireChange;
    }

    public void markInactive() {
        this.status = AdminStatus.INACTIVE;
    }

    public void markSuspended() {
        this.status = AdminStatus.SUSPENDED;
    }

    public void activate() {
        this.status = AdminStatus.ACTIVE;
    }

    public void changeRole(AdminRole role) {
        this.role = role;
    }

    public void recordSuccessfulLogin(LocalDateTime loginAt) {
        this.lastLoginAt = loginAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdminUser)) {
            return false;
        }
        AdminUser other = (AdminUser) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
