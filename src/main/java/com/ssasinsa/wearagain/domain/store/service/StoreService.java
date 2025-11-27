package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCursorListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCancelResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderListResponse;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreOrderCreateRequest;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;

public interface StoreService {

    StoreItemCursorListResponse getItems(String category, String keyword, String cursor, int size);

    StoreItemDetailResponse getItemDetail(Long itemId);

    StoreOrderCreateResponse createOrder(StoreOrderCreateRequest request, Long userId);

    StoreOrderCancelResponse cancelOrder(Long orderId, Long userId);

    StoreOrderListResponse getOrders(String cursor, int size, StoreOrderStatus status, Long userId);

    StoreOrderDetailResponse getOrderDetail(Long orderId, Long userId);
}
