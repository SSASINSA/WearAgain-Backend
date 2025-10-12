package com.ssasinsa.wearagain.auth.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_oauth_accounts",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_provider_provider_user_id", columnNames = {"provider", "provider_user_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class UserOAuthAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 64)
    private String providerUserId;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private UserOAuthAccount(AuthProvider provider, String providerUserId, String email, User user) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.user = user;
    }

    public static UserOAuthAccount create(AuthProvider provider, String providerUserId, String email, User user) {
        UserOAuthAccount account = UserOAuthAccount.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .email(email)
                .build();
        if (user != null) {
            user.addOAuthAccount(account);
        }
        return account;
    }

    public void assignUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserOAuthAccount)) {
            return false;
        }
        UserOAuthAccount other = (UserOAuthAccount) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
