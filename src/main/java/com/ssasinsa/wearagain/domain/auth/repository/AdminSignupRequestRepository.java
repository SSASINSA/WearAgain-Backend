package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminSignupRequestRepository extends JpaRepository<AdminSignupRequest, Long>, JpaSpecificationExecutor<AdminSignupRequest> {

    boolean existsByEmailAndStatusIn(String email, Collection<AdminSignupRequestStatus> statuses);

    Optional<AdminSignupRequest> findTopByEmailOrderByCreatedAtDesc(String email);

    List<AdminSignupRequest> findAllByOrderByCreatedAtDesc();

    List<AdminSignupRequest> findAllByStatusOrderByCreatedAtDesc(AdminSignupRequestStatus status);

    @Override
    @EntityGraph(attributePaths = "reviewedBy")
    Page<AdminSignupRequest> findAll(Specification<AdminSignupRequest> spec, Pageable pageable);
}
