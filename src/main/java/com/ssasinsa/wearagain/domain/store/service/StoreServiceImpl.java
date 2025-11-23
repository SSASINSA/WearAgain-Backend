package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreItemRepository storeItemRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreItemListResponse getItems(String category, String keyword, int page, int size) {
        throw new UnsupportedOperationException("스토어 기능은 구현 예정입니다.");
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemDetailResponse getItemDetail(Long itemId) {
        throw new UnsupportedOperationException("스토어 기능은 구현 예정입니다.");
    }
}
