package com.ssasinsa.wearagain.auth.domain;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column
    private String profileImageUrl;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Default
    private List<UserOAuthAccount> oauthAccounts = new ArrayList<>();

    private User(UUID id, String email, String displayName, String profileImageUrl, List<UserOAuthAccount> oauthAccounts) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.oauthAccounts = oauthAccounts;
    }

    public static User create(String email, String displayName, String profileImageUrl) {
        return User.builder()
                .email(email)
                .displayName(displayName)
                .profileImageUrl(profileImageUrl)
                .build();
    }

    public void addOAuthAccount(UserOAuthAccount account) {
        oauthAccounts.add(account);
        account.assignUser(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
