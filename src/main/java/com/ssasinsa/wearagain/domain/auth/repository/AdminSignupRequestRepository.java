package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSignupRequestRepository extends JpaRepository<AdminSignupRequest, Long> {

    boolean existsByEmailAndStatusIn(String email, Collection<AdminSignupRequestStatus> statuses);

    Optional<AdminSignupRequest> findTopByEmailOrderByCreatedAtDesc(String email);

    List<AdminSignupRequest> findAllByOrderByCreatedAtDesc();

    List<AdminSignupRequest> findAllByStatusOrderByCreatedAtDesc(AdminSignupRequestStatus status);
}
