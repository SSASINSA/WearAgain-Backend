package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
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
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import com.ssasinsa.wearagain.global.common.redis.RedisTransactionCallbackRegistrar;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

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
    @Mock
    private StoreStockService storeStockService;
    @Mock
    private RedisResourceGuard redisResourceGuard;
    @Mock
    private RedisTransactionCallbackRegistrar redisTransactionCallbackRegistrar;
    @Mock
    private TransactionOperations transactionOperations;
    @Mock
    private RedisResourceGuard.LockHandle lockHandle;

    @InjectMocks
    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionOperations.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @DisplayName("주문 생성 성공 시 재고, 크레딧 차감 및 히스토리 기록")
    @Test
    void should_create_order_when_valid_request() {
        User user = user(1L, 5000);
        StoreItem item = item(10L, 1000, 5, 2);

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(storeOrderRepository.sumQuantityByUserAndItemAndStatus(
                user,
                item,
                StoreOrderStatus.PURCHASED
        )).thenReturn(0L);
        when(storeItemRepository.decreaseStock(10L, 2, StoreItemStatus.ACTIVE)).thenReturn(1);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(storeStockService.reserve(10L, 2)).thenReturn(StoreStockService.ReserveResult.RESERVED);
        when(redisTransactionCallbackRegistrar.registerRollbackCompensation(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);
        when(storeOrderRepository.save(any(StoreOrder.class))).thenAnswer(invocation -> {
            StoreOrder order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 50L);
            ReflectionTestUtils.setField(order, "createdAt", LocalDateTime.of(2025, 2, 11, 4, 0, 0));
            return order;
        });

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 2, "강남 팝업스토어");

        StoreOrderCreateResponse response = storeService.createOrder(request, 1L);

        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerRollbackCompensation(
                eq(resourceKey),
                any(Runnable.class),
                completionCaptor.capture()
        );
        completionCaptor.getValue().run();
        assertThat(response.orderId()).isEqualTo(50L);
        assertThat(response.usedCredit()).isEqualTo(2000);
        assertThat(item.getStock()).isEqualTo(5);
        assertThat(user.getCreditBalance()).isEqualTo(3000);
        InOrder ordered = inOrder(
                storeStockService,
                transactionOperations,
                userRepository,
                storeItemRepository,
                storeOrderRepository
        );
        ordered.verify(storeStockService).reserve(10L, 2);
        ordered.verify(transactionOperations).execute(any());
        ordered.verify(userRepository).findByIdForUpdate(1L);
        ordered.verify(storeItemRepository).decreaseStock(10L, 2, StoreItemStatus.ACTIVE);
        ordered.verify(storeOrderRepository).save(any(StoreOrder.class));
        verify(storeStockService, never()).release(anyLong(), anyInt());
        verify(lockHandle).close();
        verify(creditHistoryRepository).save(any());
    }

    @DisplayName("픽업 장소가 유효하지 않으면 주문 생성 실패")
    @Test
    void should_throw_when_pickup_location_invalid() {
        User user = user(1L, 5000);
        StoreItem item = item(10L, 1000, 5, 2);

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item));
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(storeStockService.reserve(10L, 1)).thenReturn(StoreStockService.ReserveResult.RESERVED);
        when(redisTransactionCallbackRegistrar.registerRollbackCompensation(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);
        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 1, "없는 장소");

        assertThatThrownBy(() -> storeService.createOrder(request, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID.getMessage());
        ArgumentCaptor<Runnable> compensationCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerRollbackCompensation(
                eq(resourceKey),
                compensationCaptor.capture(),
                completionCaptor.capture()
        );
        compensationCaptor.getValue().run();
        completionCaptor.getValue().run();
        verify(storeStockService).reserve(10L, 1);
        verify(storeStockService).release(10L, 1);
        verify(lockHandle).close();
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

        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(user));
        when(storeOrderRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(order));
        when(storeOrderRepository.findItemIdById(77L)).thenReturn(Optional.of(10L));
        when(storeItemRepository.increaseStock(10L, 2)).thenReturn(1);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);

        StoreOrderCancelResponse response = storeService.cancelOrder(77L, 1L);

        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                actionCaptor.capture(),
                completionCaptor.capture()
        );
        actionCaptor.getValue().run();
        completionCaptor.getValue().run();
        assertThat(response.refundedCredit()).isEqualTo(2000);
        assertThat(item.getStock()).isZero();
        assertThat(user.getCreditBalance()).isEqualTo(2000);
        assertThat(order.getStatus()).isEqualTo(StoreOrderStatus.CANCELED);
        verify(storeItemRepository).increaseStock(10L, 2);
        verify(storeStockService).release(10L, 2);
        verify(lockHandle).close();
        verify(creditHistoryRepository).save(any());
    }

    @DisplayName("Redis 재고 key가 없으면 재고 부족이 아닌 일시 처리 불가 예외 발생")
    @Test
    void should_throw_unavailable_when_stock_key_missing() {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);
        StoreItem item = item(10L, 1000, 5, 2);

        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(storeStockService.reserve(10L, 1)).thenReturn(StoreStockService.ReserveResult.CACHE_MISS);
        when(storeItemRepository.findById(10L)).thenReturn(Optional.of(item));

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 1, "강남 팝업스토어");

        assertThatThrownBy(() -> storeService.createOrder(request, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_STOCK_UNAVAILABLE.getMessage());
        verify(transactionOperations, never()).execute(any());
        verify(storeStockService, never()).release(anyLong(), anyInt());
        verify(lockHandle).close();
    }

    @DisplayName("Redis 재고 key와 상품이 모두 없으면 상품 없음 예외 발생")
    @Test
    void should_throw_not_found_when_stock_key_and_item_are_missing() {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);

        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(storeStockService.reserve(10L, 1)).thenReturn(StoreStockService.ReserveResult.CACHE_MISS);
        when(storeItemRepository.findById(10L)).thenReturn(Optional.empty());

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 1, "강남 팝업스토어");

        assertThatThrownBy(() -> storeService.createOrder(request, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_NOT_FOUND.getMessage());
        verify(transactionOperations, never()).execute(any());
        verify(storeStockService, never()).release(anyLong(), anyInt());
        verify(lockHandle).close();
    }

    @DisplayName("DB transaction 실패 시 선점 재고와 lock을 즉시 반환")
    @Test
    void should_release_stock_when_database_transaction_fails() {
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);

        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(storeStockService.reserve(10L, 1)).thenReturn(StoreStockService.ReserveResult.RESERVED);
        doThrow(new StoreException(StoreErrorCode.STORE_ITEM_NOT_FOUND))
                .when(transactionOperations)
                .execute(any());

        StoreOrderCreateRequest request = new StoreOrderCreateRequest(10L, 1, "강남 팝업스토어");

        assertThatThrownBy(() -> storeService.createOrder(request, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_NOT_FOUND.getMessage());
        verify(storeStockService).release(10L, 1);
        verify(lockHandle).close();
        verifyNoInteractions(creditHistoryRepository);
    }

    @DisplayName("취소 DB transaction 실패 시 Redis 재고를 반환하지 않음")
    @Test
    void should_not_release_stock_when_cancel_transaction_fails() {
        User user = user(1L, 0);
        StoreItem item = item(10L, 1000, 0, 2);
        StoreOrder order = StoreOrder.create(user, item, 1000, 2, "강남 팝업스토어");
        order.cancel();
        ReflectionTestUtils.setField(order, "id", 77L);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(10L);

        when(storeOrderRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(order));
        when(storeOrderRepository.findItemIdById(77L)).thenReturn(Optional.of(10L));
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);

        assertThatThrownBy(() -> storeService.cancelOrder(77L, 1L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ORDER_CANCEL_INVALID.getMessage());
        assertThat(item.getStock()).isZero();
        assertThat(user.getCreditBalance()).isZero();
        assertThat(order.getStatus()).isEqualTo(StoreOrderStatus.CANCELED);
        verify(storeStockService, never()).release(anyLong(), anyInt());
        verify(lockHandle).close();
        verifyNoInteractions(creditHistoryRepository);
    }

    @DisplayName("스토어 상품 목록 커서 조회 시 ACTIVE만 반환")
    @Test
    void should_get_active_items_only() {
        StoreItem active = item(10L, 1000, 5, 2);
        StoreItem inactive = item(11L, 1200, 5, 2);
        inactive.changeStatus(StoreItemStatus.INACTIVE);

        when(storeItemRepository.findActiveItemsWithCursor(
                eq(StoreItemStatus.ACTIVE),
                eq(null),
                eq(null),
                eq(null),
                any(Pageable.class)
        ))
                .thenReturn(List.of(active, inactive));
        when(storeItemImageRepository.findThumbnailsByStoreItemIds(List.of(10L)))
                .thenReturn(List.of());

        var response = storeService.getItems(null, null, null, 1);

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).id()).isEqualTo(10L);
    }

    private StoreItem item(Long id, int price, int stock, Integer maxPurchasePerUser) {
        StoreItem item = StoreItem.create(
                "name",
                "desc",
                "cat",
                price,
                stock,
                maxPurchasePerUser,
                StoreItemStatus.ACTIVE,
                List.of(),
                List.of("강남 팝업스토어", "홍대 매장")
        );
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
