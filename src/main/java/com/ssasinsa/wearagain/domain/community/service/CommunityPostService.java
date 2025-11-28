package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostsRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.KeywordsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;

public interface CommunityPostService {

    PostsResponse getPosts(PostsRequest request, Long userId);

    PostDetailResponse getPostDetail(Long postId, Long userId);

    void createPost(PostCreateRequest request, Long userId);

    void updatePost(Long postId, PostUpdateRequest request, Long userId);

    void deletePost(Long postId, Long userId);

    PostsResponse getMyPosts(Long cursor, Integer limit, Long userId);

    PostsResponse getMyCommentedPosts(Long cursor, Integer limit, Long userId);

    KeywordsResponse getKeywords();
}

