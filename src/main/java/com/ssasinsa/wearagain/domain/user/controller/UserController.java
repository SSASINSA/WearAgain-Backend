package com.ssasinsa.wearagain.domain.user.controller;

import com.ssasinsa.wearagain.domain.community.dto.response.PostsResponse;
import com.ssasinsa.wearagain.domain.community.service.CommunityPostService;
import com.ssasinsa.wearagain.domain.user.docs.UserApiDocs;
import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.domain.user.service.UserSummaryService;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = UserApiDocs.USER_TAG_NAME, description = UserApiDocs.USER_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class UserController {

    private final UserSummaryService userSummaryService;
    private final CommunityPostService communityPostService;

    @UserApiDocs.GetUserSummary
    @GetMapping("/summary")
    public ResponseEntity<UserSummaryResponse> getUserSummary(@AuthenticationPrincipal AuthenticatedUser user) {
        UserSummaryResponse response = userSummaryService.getUserSummary(user.userId());
        return ResponseEntity.ok(response);
    }

    @UserApiDocs.GetMyPosts
    @GetMapping("/posts")
    public ResponseEntity<PostsResponse> getMyPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        PostsResponse response = communityPostService.getMyPosts(cursor, limit, user.userId());
        return ResponseEntity.ok(response);
    }

    @UserApiDocs.GetMyCommentedPosts
    @GetMapping("/comments")
    public ResponseEntity<PostsResponse> getMyCommentedPosts(
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) Integer limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        PostsResponse response = communityPostService.getMyCommentedPosts(cursor, limit, user.userId());
        return ResponseEntity.ok(response);
    }
}
