package com.ssasinsa.wearagain.domain.user.controller;

import com.ssasinsa.wearagain.domain.user.docs.UserApiDocs;
import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.domain.user.service.UserSummaryService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = UserApiDocs.USER_TAG_NAME, description = UserApiDocs.USER_TAG_DESCRIPTION)
@RequiredArgsConstructor
public class UserController {

    private final UserSummaryService userSummaryService;

    @UserApiDocs.GetUserSummary
    @GetMapping("/summary")
    public ResponseEntity<UserSummaryResponse> getUserSummary(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        UserSummaryResponse response = userSummaryService.getUserSummary(user.userId());
        return ResponseEntity.ok(response);
    }
}
