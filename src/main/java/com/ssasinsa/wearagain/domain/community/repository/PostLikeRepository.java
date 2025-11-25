package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);
}

