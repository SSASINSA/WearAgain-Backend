package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;

public interface CommunityPostService {

    PostDetailResponse getPostDetail(Long postId, Long userId);

    void createPost(PostCreateRequest request, Long userId);

    void updatePost(Long postId, PostUpdateRequest request, Long userId);

    void deletePost(Long postId, Long userId);
}

