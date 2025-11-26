package com.ssasinsa.wearagain.domain.community.controller;

import com.ssasinsa.wearagain.domain.community.docs.CommunityApiDocs;
import com.ssasinsa.wearagain.domain.community.dto.request.ReportRequest;
import com.ssasinsa.wearagain.domain.community.service.ReportService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/community/reports")
@Tag(name = CommunityApiDocs.TAG_NAME, description = CommunityApiDocs.TAG_DESCRIPTION)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @CommunityApiDocs.ReportPost
    @PostMapping
    public ResponseEntity<Void> reportPost(
            @Valid @RequestBody ReportRequest request,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        reportService.reportPost(request, user.userId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

