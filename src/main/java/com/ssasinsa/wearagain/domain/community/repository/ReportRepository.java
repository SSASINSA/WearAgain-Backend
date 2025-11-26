package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.Report;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByTargetTypeAndTargetIdAndReporterId(String targetType, Long targetId, Long reporterId);
}

