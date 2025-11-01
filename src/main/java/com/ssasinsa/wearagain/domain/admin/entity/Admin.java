package com.ssasinsa.wearagain.domain.admin.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "admins")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admins_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdminRole role;

    @Builder(access = AccessLevel.PRIVATE)
    private Admin(String name, String email, String password, AdminRole role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role == null ? AdminRole.ADMIN : role;
    }

    public static Admin create(String name, String email, String password, AdminRole role) {
        return Admin.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(role)
                .build();
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void changeRole(AdminRole role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Admin)) {
            return false;
        }
        Admin other = (Admin) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
