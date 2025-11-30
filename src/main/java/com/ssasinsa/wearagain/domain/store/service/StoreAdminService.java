package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;

public interface StoreAdminService {

    StoreItemCreateResponse createItem(StoreItemCreateRequest request, Long adminId);

    StoreItemListResponse getItems(String status, String category, String keyword, String keywordScope, String sort, int page, int size);

    StoreItemDetailResponse getItemDetail(Long itemId);

    StoreItemDetailResponse updateItem(Long itemId, StoreItemUpdateRequest request, Long adminId);

    StoreItemDetailResponse updateItemStatus(Long itemId, StoreItemStatusUpdateRequest request, Long adminId);

    void deleteItem(Long itemId, Long adminId);
}
