package com.ssasinsa.wearagain.domain.growth.controller;

import com.ssasinsa.wearagain.domain.growth.docs.GrowthApiDocs;
import com.ssasinsa.wearagain.domain.growth.dto.GrowthStatusResponse;
import com.ssasinsa.wearagain.domain.growth.dto.MagicScissorUseRequest;
import com.ssasinsa.wearagain.domain.growth.dto.MagicScissorUseResponse;
import com.ssasinsa.wearagain.domain.growth.dto.MagicScissorUseResult;
import com.ssasinsa.wearagain.domain.growth.service.GrowthCommandService;
import com.ssasinsa.wearagain.domain.growth.service.GrowthQueryService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/growth")
@RequiredArgsConstructor
@Tag(name = GrowthApiDocs.TAG_NAME, description = GrowthApiDocs.TAG_DESCRIPTION)
public class GrowthController {

    private static final int NEXT_LEVEL_EXP = 100;

    private final GrowthQueryService growthQueryService;
    private final GrowthCommandService growthCommandService;

    @GrowthApiDocs.GetStatus
    @GetMapping("/status")
    public ResponseEntity<GrowthStatusResponse> getStatus(@AuthenticationPrincipal AuthenticatedUser user) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        GrowthStatusResponse response = GrowthStatusResponse.of(growthQueryService.getStatus(user.userId()));
        return ResponseEntity.ok(response);
    }

    @GrowthApiDocs.UseMagicScissors
    @PostMapping("/magic-scissors/use")
    public ResponseEntity<MagicScissorUseResponse> useMagicScissors(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody MagicScissorUseRequest request
    ) {
        if (user == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        MagicScissorUseResult result = growthCommandService.useMagicScissors(user.userId(), request.useCount());
        MagicScissorUseResponse response = MagicScissorUseResponse.from(result, NEXT_LEVEL_EXP);
        return ResponseEntity.ok(response);
    }
}
