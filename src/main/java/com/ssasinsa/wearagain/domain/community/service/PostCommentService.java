package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.request.CommentCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;

public interface PostCommentService {

    void createComment(Long postId, CommentCreateRequest request, Long userId);

    void updateComment(Long postId, Long commentId, CommentUpdateRequest request, Long userId);

    void deleteComment(Long postId, Long commentId, Long userId);

    CommentsResponse getComments(Long postId, CommentsRequest request, Long userId);
}

