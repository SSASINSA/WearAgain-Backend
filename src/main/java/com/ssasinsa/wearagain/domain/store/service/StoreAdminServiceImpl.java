package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreAdminServiceImpl implements StoreAdminService {

    private final StoreItemRepository storeItemRepository;
    private final StoreItemImageRepository storeItemImageRepository;

    @Override
    @Transactional
    public StoreItemCreateResponse createItem(StoreItemCreateRequest request, Long adminId) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemListResponse getItems(String status, String category, String keyword, int page, int size) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemDetailResponse getItemDetail(Long itemId) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional
    public StoreItemDetailResponse updateItem(Long itemId, StoreItemUpdateRequest request, Long adminId) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional
    public StoreItemDetailResponse updateItemStatus(Long itemId, StoreItemStatusUpdateRequest request, Long adminId) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long adminId) {
        throw new UnsupportedOperationException("스토어 관리 기능은 구현 예정입니다.");
    }
}
