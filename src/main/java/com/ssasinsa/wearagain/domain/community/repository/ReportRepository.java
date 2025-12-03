package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByTargetTypeAndTargetIdAndReporterId(String targetType, Long targetId, Long reporterId);

    @Query("""
            SELECT r.targetId, COUNT(r)
            FROM Report r
            WHERE r.targetType = :targetType AND r.targetId IN :targetIds
            GROUP BY r.targetId
            """)
    List<Object[]> countReportsByTargetTypeAndTargetIds(
            @Param("targetType") String targetType,
            @Param("targetIds") List<Long> targetIds
    );

    long countByTargetTypeAndTargetId(String targetType, Long targetId);
}

