package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.AuthProvider;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.entity.UserOAuthAccount;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, Long> {

    Optional<UserOAuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    void deleteAllByUser(User user);
}
