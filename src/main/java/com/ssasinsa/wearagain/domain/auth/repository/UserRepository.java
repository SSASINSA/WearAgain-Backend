package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findAllBySuspended(boolean suspended, Pageable pageable);

    @Query("select coalesce(sum(u.ticketBalance), 0) from User u")
    long sumTicketBalance();

    @Query("select coalesce(sum(u.creditBalance), 0) from User u")
    long sumCreditBalance();

    long countByCreatedAtBefore(LocalDateTime before);
}
