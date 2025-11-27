package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreOrderCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCancelResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderCreateResponse;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreOrderRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @Mock
    private StoreItemRepository storeItemRepository;
    @Mock
    private StoreItemImageRepository storeItemImageRepository;
    @Mock
    private StoreOrderRepository storeOrderRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CreditHistoryRepository creditHistoryRepository;

    @InjectMocks
    private StoreServiceImpl storeService;

    @DisplayName("주문 생성 성공 시 재고, 크레딧 차감 및 히스토리 기록")
    @Test
    void should_create_order_when_valid_request() {
        User user = user(1L, 5000);
        StoreItem item = item(10L, 1000, 5, 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(storeOrderRepository.countByUserAndItemAndStatus(user, item, StoreOrderStatus.PURCHASED)).thenReturn(0L);
        when(storeOrderRepository.save(any(StoreOrder.class))).thenAnswer(invocation -> {
            StoreOrder order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 50L);
            ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2025, 2, 11, 4, 0, 0));
            return order;
        });

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 2, "강남 팝업스토어");

        StoreOrderCreateResponse response = storeService.createOrder(request, 1L);

        assertThat(response.orderId()).isEqualTo(50L);
        assertThat(response.usedCredit()).isEqualTo(2000);
        assertThat(item.getStock()).isEqualTo(3);
        assertThat(user.getCreditBalance()).isEqualTo(3000);
        verify(creditHistoryRepository).save(any());
    }

    @DisplayName("픽업 장소가 유효하지 않으면 주문 생성 실패")
    @Test
    void should_throw_when_pickup_location_invalid() {
        User user = user(1L, 5000);
        StoreItem item = item(10L, 1000, 5, 2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item));

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 1, "없는 장소");

        assertThatThrownBy(() -> storeService.createOrder(request, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID.getMessage());
    }

    @DisplayName("주문 취소 시 재고/크레딧 복원 및 상태 변경")
    @Test
    void should_cancel_order_and_refund() {
        User user = user(1L, 0);
        StoreItem item = item(10L, 1000, 0, 2);
        StoreOrder order = StoreOrder.create(user, item, 1000, 2, "강남 팝업스토어");
        ReflectionTestUtils.setField(order, "id", 77L);
        ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2025, 2, 11, 4, 0, 0));
        ReflectionTestUtils.setField(order, "updatedAt", LocalDateTime.of(2025, 2, 11, 4, 0, 0));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(storeOrderRepository.findById(77L)).thenReturn(Optional.of(order));

        StoreOrderCancelResponse response = storeService.cancelOrder(77L, 1L);

        assertThat(response.refundedCredit()).isEqualTo(2000);
        assertThat(item.getStock()).isEqualTo(2);
        assertThat(user.getCreditBalance()).isEqualTo(2000);
        assertThat(order.getStatus()).isEqualTo(StoreOrderStatus.CANCELED);
        verify(creditHistoryRepository).save(any());
    }

    @DisplayName("스토어 상품 목록 커서 조회 시 ACTIVE만 반환")
    @Test
    void should_get_active_items_only() {
        StoreItem active = item(10L, 1000, 5, 2);
        StoreItem inactive = item(11L, 1200, 5, 2);
        inactive.changeStatus(StoreItemStatus.INACTIVE);

        when(storeItemRepository.findActiveItemsWithCursor(eq(StoreItemStatus.ACTIVE), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(List.of(active, inactive));
        when(storeItemImageRepository.findThumbnailsByStoreItemIds(List.of(10L)))
                .thenReturn(List.of());

        var response = storeService.getItems(null, null, null, 1);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo(10L);
    }

    private StoreItem item(Long id, int price, int stock, Integer maxPurchasePerUser) {
        StoreItem item = StoreItem.create("name", "desc", "cat", price, stock, maxPurchasePerUser, StoreItemStatus.ACTIVE, List.of(), List.of("강남 팝업스토어", "홍대 매장"));
        ReflectionTestUtils.setField(item, "id", id);
        ReflectionTestUtils.setField(item, "createdAt", LocalDateTime.now(ZoneOffset.UTC));
        ReflectionTestUtils.setField(item, "updatedAt", LocalDateTime.now(ZoneOffset.UTC));
        return item;
    }

    private User user(Long id, int creditBalance) {
        User user = User.create("user@test.com", "유저", null);
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "creditBalance", creditBalance);
        return user;
    }
}
