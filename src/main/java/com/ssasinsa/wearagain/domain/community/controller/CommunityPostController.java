package com.ssasinsa.wearagain.domain.community.controller;

import com.ssasinsa.wearagain.domain.community.docs.CommunityApiDocs;
import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostsRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.domain.community.service.CommunityPostService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
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
@RequestMapping("/api/v1/community/posts")
@Tag(name = CommunityApiDocs.TAG_NAME, description = CommunityApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    @CommunityApiDocs.GetPosts
    @GetMapping
    public ResponseEntity<PostsResponse> getPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user != null ? user.userId() : null;
        PostsRequest request = new PostsRequest(cursor, limit, keyword);
        PostsResponse response = communityPostService.getPosts(request, userId);
        return ResponseEntity.ok(response);
    }

    @CommunityApiDocs.GetPostDetail
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long userId = user != null ? user.userId() : null;
        PostDetailResponse response = communityPostService.getPostDetail(postId, userId);
        return ResponseEntity.ok(response);
    }

    @CommunityApiDocs.CreatePost
    @PostMapping
    public ResponseEntity<Void> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        communityPostService.createPost(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @CommunityApiDocs.UpdatePost
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        communityPostService.updatePost(postId, request, user.userId());
        return ResponseEntity.ok().build();
    }

    @CommunityApiDocs.DeletePost
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        communityPostService.deletePost(postId, user.userId());
        return ResponseEntity.ok().build();
    }
}

