package com.ssasinsa.wearagain.auth.domain.repository;

import com.ssasinsa.wearagain.auth.domain.AuthProvider;
import com.ssasinsa.wearagain.auth.domain.UserOAuthAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    Optional<UserOAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
