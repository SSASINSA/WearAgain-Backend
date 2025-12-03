package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListRequest;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListResponse;

public interface PostAdminService {

    PostAdminListResponse getPosts(PostAdminListRequest request);

    PostAdminDetailResponse getPostDetail(Long postId);
}
