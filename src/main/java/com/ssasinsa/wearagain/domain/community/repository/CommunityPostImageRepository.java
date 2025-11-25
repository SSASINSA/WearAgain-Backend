package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {

    void deleteByPost(CommunityPost post);
}

