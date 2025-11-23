package com.ssasinsa.wearagain.domain.store.controller;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.store.docs.StoreApiDocs;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreImageUploadResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.service.StoreAdminService;
import com.ssasinsa.wearagain.domain.store.service.StoreImageUploadService;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = StoreApiDocs.TAG_NAME, description = StoreApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/api/v1/admin/store")
@RequiredArgsConstructor
public class StoreAdminController {

    private final StoreAdminService storeAdminService;
    private final StoreImageUploadService storeImageUploadService;

    @StoreApiDocs.UploadItemImage
    @PostMapping(value = "/items/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StoreImageUploadResponse> uploadItemImage(@RequestPart("file") MultipartFile file) {
        String imageName = storeImageUploadService.uploadImage(file);
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/upload/")
                .path(imageName)
                .toUriString();
        StoreImageUploadResponse response = new StoreImageUploadResponse(imageName, imageUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @StoreApiDocs.CreateItem
    @PostMapping("/items")
    public ResponseEntity<StoreItemCreateResponse> createItem(
            @Valid @RequestBody StoreItemCreateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        StoreItemCreateResponse response = storeAdminService.createItem(request, principal.adminId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @StoreApiDocs.GetAdminItems
    @GetMapping("/items")
    public ResponseEntity<StoreItemListResponse> getItems(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        StoreItemListResponse response = storeAdminService.getItems(status, category, keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.GetAdminItemDetail
    @GetMapping("/items/{itemId}")
    public ResponseEntity<StoreItemDetailResponse> getItemDetail(@PathVariable Long itemId) {
        StoreItemDetailResponse response = storeAdminService.getItemDetail(itemId);
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.UpdateItem
    @PutMapping("/items/{itemId}")
    public ResponseEntity<StoreItemDetailResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody StoreItemUpdateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        StoreItemDetailResponse response = storeAdminService.updateItem(itemId, request, principal.adminId());
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.UpdateItemStatus
    @PatchMapping("/items/{itemId}/status")
    public ResponseEntity<StoreItemDetailResponse> updateItemStatus(
            @PathVariable Long itemId,
            @Valid @RequestBody StoreItemStatusUpdateRequest request,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        StoreItemDetailResponse response = storeAdminService.updateItemStatus(itemId, request, principal.adminId());
        return ResponseEntity.ok(response);
    }

    @StoreApiDocs.DeleteItem
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal AdminAuthenticatedUser principal
    ) {
        ensureAuthenticated(principal);
        storeAdminService.deleteItem(itemId, principal.adminId());
        return ResponseEntity.noContent().build();
    }

    private void ensureAuthenticated(AdminAuthenticatedUser principal) {
        if (principal == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
    }
}
