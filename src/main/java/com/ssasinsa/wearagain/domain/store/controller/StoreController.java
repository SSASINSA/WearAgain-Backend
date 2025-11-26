package com.ssasinsa.wearagain.domain.store.controller;

import com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCursorListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.service.StoreService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.security.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = StoreApiDocs.TAG_NAME, description = StoreApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @StoreApiDocs.GetItems
    @GetMapping("/items")
    public ResponseEntity<StoreItemCursorListResponse> getItems(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "cursor", required = false) String cursor,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        StoreItemCursorListResponse response = storeService.getItems(category, keyword, cursor, size);
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.GetItemDetail
    @GetMapping("/items/{itemId}")
    public ResponseEntity<StoreItemDetailResponse> getItemDetail(
            @PathVariable Long itemId,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        StoreItemDetailResponse response = storeService.getItemDetail(itemId);
        return ResponseEntity.ok(response);
    }

    private void ensureAuthenticated(AuthenticatedUser principal) {
        if (principal == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
