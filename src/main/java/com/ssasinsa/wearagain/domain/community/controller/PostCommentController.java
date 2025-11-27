package com.ssasinsa.wearagain.domain.community.controller;

import com.ssasinsa.wearagain.domain.community.docs.CommunityApiDocs;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.CommentsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommentsResponse;
import com.ssasinsa.wearagain.domain.community.service.PostCommentService;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/community/posts/{postId}/comments")
@Tag(name = CommunityApiDocs.TAG_NAME, description = CommunityApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class PostCommentController {

    private final PostCommentService postCommentService;

    @CommunityApiDocs.CreateComment
    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        postCommentService.createComment(postId, request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @CommunityApiDocs.UpdateComment
    @PutMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        postCommentService.updateComment(postId, commentId, request, user.userId());
        return ResponseEntity.ok().build();
    }

    @CommunityApiDocs.DeleteComment
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        postCommentService.deleteComment(postId, commentId, user.userId());
        return ResponseEntity.ok().build();
    }

    @CommunityApiDocs.GetComments
    @GetMapping
    public ResponseEntity<CommentsResponse> getComments(
            @PathVariable Long postId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user != null ? user.userId() : null;
        CommentsRequest request = new CommentsRequest(cursor, limit);
        CommentsResponse response = postCommentService.getComments(postId, request, userId);
        return ResponseEntity.ok(response);
    }
}

