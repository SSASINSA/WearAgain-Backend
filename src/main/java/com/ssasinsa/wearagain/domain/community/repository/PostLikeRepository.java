package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.PostLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Query("SELECT l.post.id FROM PostLike l WHERE l.post.id IN :postIds AND l.user.id = :userId")
    List<Long> findLikedPostIdsByPostIdsAndUserId(@Param("postIds") List<Long> postIds, @Param("userId") Long userId);
}

