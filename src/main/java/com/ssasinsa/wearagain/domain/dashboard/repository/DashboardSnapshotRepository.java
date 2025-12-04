package com.ssasinsa.wearagain.domain.dashboard.repository;

import com.ssasinsa.wearagain.domain.dashboard.entity.DashboardSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardSnapshotRepository extends JpaRepository<DashboardSnapshot, Long> {

    Optional<DashboardSnapshot> findTopByOrderByCreatedAtDesc();
}
