package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse.CommentItem;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostComment;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.PostCommentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createComment(Long postId, CommentCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.INVALID_POST_DATA));

        CommunityPost post = communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        PostComment comment = PostComment.create(post, user, request.content());
        postCommentRepository.save(comment);

        log.info("댓글 생성 완료: commentId={}, postId={}, userId={}", comment.getId(), postId, userId);
    }

    @Override
    @Transactional
    public void updateComment(Long postId, Long commentId, CommentUpdateRequest request, Long userId) {
        communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        PostComment comment = postCommentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new CommunityException(CommunityErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new CommunityException(CommunityErrorCode.COMMENT_UPDATE_FORBIDDEN);
        }

        comment.updateContent(request.content());

        log.info("댓글 수정 완료: commentId={}, postId={}, userId={}", commentId, postId, userId);
    }

    @Override
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        PostComment comment = postCommentRepository.findByIdAndActiveTrue(commentId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPost().getId().equals(postId)) {
            throw new CommunityException(CommunityErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getUser().getId().equals(userId)) {
            throw new CommunityException(CommunityErrorCode.COMMENT_DELETE_FORBIDDEN);
        }

        comment.deactivate();

        log.info("댓글 삭제 완료: commentId={}, postId={}, userId={}", commentId, postId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentsResponse getComments(Long postId, CommentsRequest request, Long userId) {
        // 게시글 존재 확인
        communityPostRepository.findByIdAndActiveTrue(postId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        int limit = request.limit() != null ? request.limit() : 10;
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        // 1단계: Comment ID만 먼저 조회
        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Long> commentIds = postCommentRepository.findCommentIdsByPostId(
                postId,
                request.cursor(),
                pageable
        );

        if (commentIds.isEmpty()) {
            return new CommentsResponse(limit, null, false, new ArrayList<>());
        }

        boolean hasNext = commentIds.size() > limit;
        List<Long> limitedIds = hasNext ? commentIds.subList(0, limit) : commentIds;

        // 2단계: ID 목록으로 Comment + user fetch join
        List<PostComment> comments = postCommentRepository.findCommentsByIds(limitedIds);

        List<CommentItem> commentItems = comments.stream()
                .map(comment -> {
                    boolean isMine = userId != null && userId.equals(comment.getUser().getId());
                    return new CommentItem(
                            comment.getId(),
                            new CommentItem.AuthorInfo(
                                    comment.getUser().getId(),
                                    comment.getUser().getDisplayName()
                            ),
                            comment.getCreatedAt(),
                            comment.getContent(),
                            isMine
                    );
                })
                .toList();

        String nextCursor = hasNext && !limitedIds.isEmpty()
                ? String.valueOf(limitedIds.get(limitedIds.size() - 1))
                : null;

        return new CommentsResponse(limit, nextCursor, hasNext, commentItems);
    }
}

