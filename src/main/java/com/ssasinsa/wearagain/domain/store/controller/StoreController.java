package com.ssasinsa.wearagain.domain.store.controller;

import com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<StoreItemListResponse> getItems(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        StoreItemListResponse response = storeService.getItems(category, keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.GetItemDetail
    @GetMapping("/items/{itemId}")
    public ResponseEntity<StoreItemDetailResponse> getItemDetail(@PathVariable Long itemId) {
        StoreItemDetailResponse response = storeService.getItemDetail(itemId);
        return ResponseEntity.ok(response);
    }
}
