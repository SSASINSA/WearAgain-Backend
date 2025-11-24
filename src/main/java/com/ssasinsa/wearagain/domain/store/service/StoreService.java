package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;

public interface StoreService {

    StoreItemListResponse getItems(String category, String keyword, int page, int size);

    StoreItemDetailResponse getItemDetail(Long itemId);
}
