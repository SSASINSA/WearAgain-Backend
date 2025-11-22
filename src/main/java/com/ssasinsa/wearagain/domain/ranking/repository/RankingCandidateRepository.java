package com.ssasinsa.wearagain.domain.ranking.repository;

import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistoryReason;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
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
    List<RankingCandidate> findCandidates(@Param("useReason") MagicScissorHistoryReason useReason, Pageable pageable);

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
            where ug.totalScissorUsed > 0 and ug.user.id = :userId
            group by ug.user.id, u.displayName, ug.totalScissorUsed, ug.createdAt
            """)
    Optional<RankingCandidate> findCandidateByUserId(@Param("useReason") MagicScissorHistoryReason useReason, @Param("userId") Long userId);

    @Query(value = """
            select ranked.rankPos
            from (
                select ug.users_id as userId,
                       row_number() over (
                           order by ug.total_scissor_used desc,
                                    coalesce(max(msh.created_at), ug.created_at) asc,
                                    ug.users_id asc
                       ) as rankPos
                from user_growths ug
                left join magic_scissor_histories msh
                    on msh.user_growths_id = ug.user_growths_id and msh.reason = :reason
                where ug.total_scissor_used > 0
                group by ug.users_id, ug.total_scissor_used, ug.created_at
            ) ranked
            where ranked.userId = :userId
            """, nativeQuery = true)
    Integer findRankForUser(@Param("reason") String reason, @Param("userId") Long userId);
}
