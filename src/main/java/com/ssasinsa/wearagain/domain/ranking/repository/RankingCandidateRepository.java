package com.ssasinsa.wearagain.domain.ranking.repository;

import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistoryReason;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface RankingCandidateRepository extends Repository<UserGrowth, Long> {

    @Query("""
            select new com.ssasinsa.wearagain.domain.ranking.repository.RankingCandidate(
                ug.user.id,
                u.displayName,
                ug.totalScissorUsed,
                coalesce(max(msh.createdAt), ug.createdAt)
            )
            from UserGrowth ug
            join ug.user u
            left join com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistory msh
                on msh.userGrowth = ug and msh.reason = :useReason
            where ug.totalScissorUsed > 0
            group by ug.user.id, u.displayName, ug.totalScissorUsed, ug.createdAt
            order by ug.totalScissorUsed desc, coalesce(max(msh.createdAt), ug.createdAt) asc, ug.user.id asc
            """)
    List<RankingCandidate> findCandidates(@Param("useReason") MagicScissorHistoryReason useReason);
}
