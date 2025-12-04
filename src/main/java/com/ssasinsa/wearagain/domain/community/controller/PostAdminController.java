package com.ssasinsa.wearagain.domain.community.controller;

import com.ssasinsa.wearagain.domain.community.docs.PostAdminApiDocs;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminDetailResponse;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListRequest;
import com.ssasinsa.wearagain.domain.community.dto.admin.PostAdminListResponse;
import com.ssasinsa.wearagain.domain.community.service.PostAdminService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/posts")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
@Tag(name = PostAdminApiDocs.TAG_NAME, description = PostAdminApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class PostAdminController {

    private final PostAdminService postAdminService;

    @PostAdminApiDocs.GetPosts
    @GetMapping
    public ResponseEntity<PostAdminListResponse> getPosts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String keywordScope,
            @RequestParam(required = false) String sort
    ) {
        PostAdminListRequest request = PostAdminListRequest.of(page, size, status, keyword, keywordScope, sort);
        return ResponseEntity.ok(postAdminService.getPosts(request));
    }

    @PostAdminApiDocs.GetPostDetail
    @GetMapping("/{postId}")
    public ResponseEntity<PostAdminDetailResponse> getPostDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(postAdminService.getPostDetail(postId));
    }

    @PostAdminApiDocs.DeletePost
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postAdminService.deletePost(postId);
        return ResponseEntity.ok().build();
    }
}
