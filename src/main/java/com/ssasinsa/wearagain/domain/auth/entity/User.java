package com.ssasinsa.wearagain.domain.auth.entity;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PRIVATE)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "users_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @Column(name = "profile_image_url", length = 512)
    private String profileImageUrl;

    @Column(name = "ticket_balance", nullable = false)
    @Default
    private int ticketBalance = 0;

    @Column(name = "credit_balance", nullable = false)
    @Default
    private int creditBalance = 0;

    @Column(name = "is_suspended", nullable = false)
    @Default
    private boolean suspended = false;

    @Column(name = "is_deleted", nullable = false)
    @Default
    private boolean deleted = false;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Default
    private List<UserOAuthAccount> oauthAccounts = new ArrayList<>();

    private User(
            Long id,
            String email,
            String displayName,
            String profileImageUrl,
            int ticketBalance,
            int creditBalance,
            boolean suspended,
            boolean deleted,
            List<UserOAuthAccount> oauthAccounts
    ) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
        this.ticketBalance = ticketBalance;
        this.creditBalance = creditBalance;
        this.suspended = suspended;
        this.deleted = deleted;
        this.oauthAccounts = oauthAccounts == null ? new ArrayList<>() : oauthAccounts;
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

    public int increaseTicketBalance(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.ticketBalance += amount;
        return this.ticketBalance;
    }

    public int decreaseTicketBalance(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (this.ticketBalance < amount) {
            throw new IllegalStateException("insufficient ticket balance");
        }
        this.ticketBalance -= amount;
        return this.ticketBalance;
    }

    public int increaseCreditBalance(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.creditBalance += amount;
        return this.creditBalance;
    }

    public int decreaseCreditBalance(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (this.creditBalance < amount) {
            throw new IllegalStateException("insufficient credit balance");
        }
        this.creditBalance -= amount;
        return this.creditBalance;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateTicketBalance(int ticketBalance) {
        if (ticketBalance < 0) {
            throw new IllegalArgumentException("ticketBalance must be non-negative");
        }
        this.ticketBalance = ticketBalance;
    }

    public void updateCreditBalance(int creditBalance) {
        if (creditBalance < 0) {
            throw new IllegalArgumentException("creditBalance must be non-negative");
        }
        this.creditBalance = creditBalance;
    }

    public void updateSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void withdraw(String anonymizedEmail, String withdrawnDisplayName) {
        if (!StringUtils.hasText(anonymizedEmail) || !StringUtils.hasText(withdrawnDisplayName)) {
            throw new IllegalArgumentException("anonymizedEmail and withdrawnDisplayName must not be blank");
        }
        this.email = anonymizedEmail;
        this.displayName = withdrawnDisplayName;
        this.profileImageUrl = null;
        this.ticketBalance = 0;
        this.creditBalance = 0;
        this.suspended = false;
        this.deleted = true;
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
