package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
