package com.ssasinsa.wearagain.domain.community.controller;

import com.ssasinsa.wearagain.domain.community.docs.CommunityApiDocs;
import com.ssasinsa.wearagain.domain.community.dto.request.PostCreateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostUpdateRequest;
import com.ssasinsa.wearagain.domain.community.dto.request.PostsRequest;
import com.ssasinsa.wearagain.domain.community.dto.response.CommunityImageUploadResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.KeywordsResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostLikeResponse;
import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.service.CommunityPostService;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import com.ssasinsa.wearagain.global.storage.ImageStorageErrorCode;
import com.ssasinsa.wearagain.global.storage.ImageStorageException;
import com.ssasinsa.wearagain.global.storage.ImageStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Validated
@RestController
@RequestMapping("/api/v1/community/posts")
@Tag(name = CommunityApiDocs.TAG_NAME, description = CommunityApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class CommunityPostController {

    private final CommunityPostService communityPostService;
    private final ImageStorageService imageStorageService;

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

    @CommunityApiDocs.UploadImage
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommunityImageUploadResponse> uploadPostImage(@RequestPart("file") MultipartFile file) {
        String imageName;
        try {
            imageName = imageStorageService.store(file);
        } catch (ImageStorageException exception) {
            if (exception.getErrorCode() == ImageStorageErrorCode.INVALID_FILE) {
                throw new CommunityException(CommunityErrorCode.INVALID_IMAGE_INFORMATION, exception);
            }
            throw new CommunityException(CommunityErrorCode.IMAGE_UPLOAD_FAILED, exception);
        }
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(imageName)
                .toUriString();
        CommunityImageUploadResponse response = new CommunityImageUploadResponse(imageName, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @CommunityApiDocs.GetKeywords
    @GetMapping("/keywords")
    public ResponseEntity<KeywordsResponse> getKeywords() {
        KeywordsResponse response = communityPostService.getKeywords();
        return ResponseEntity.ok(response);
    }

    @CommunityApiDocs.ToggleLike
    @PostMapping("/{postId}/likes")
    public ResponseEntity<PostLikeResponse> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        PostLikeResponse response = communityPostService.toggleLike(postId, user.userId());
        return ResponseEntity.ok(response);
    }
}

