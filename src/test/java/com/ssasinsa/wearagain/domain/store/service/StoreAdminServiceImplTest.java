package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest.StoreItemImageRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreAdminOrderCancelResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreAdminOrderListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
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
import java.time.Duration;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAdminServiceImplTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private StoreItemImageRepository storeItemImageRepository;

    @Mock
    private StoreOrderRepository storeOrderRepository;

    @Mock
    private CreditHistoryRepository creditHistoryRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

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
    private StoreAdminServiceImpl storeAdminService;

    @BeforeEach
    void setUp() {
        lenient().when(transactionOperations.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
    }

    @DisplayName("상품 상태 변경 요청에 status가 없으면 예외 발생")
    @Test
    void should_throw_when_status_null_on_update_status() throws Exception {
        StoreItem item = StoreItem.create(
                "name",
                "desc",
                "cat",
                1000,
                0,
                1,
                StoreItemStatus.ACTIVE,
                List.of(),
                List.of("강남")
        );
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(adminUserRepository.findById(10L)).thenReturn(java.util.Optional.of(admin()));
        when(redisResourceGuard.tryAcquireWrite(
                eq(RedisResourceKey.storeItem(1L)),
                any(Duration.class)
        )).thenReturn(Optional.of(lockHandle));

        StoreItemStatusUpdateRequest request = new StoreItemStatusUpdateRequest(null);

        assertThatThrownBy(() -> storeAdminService.updateItemStatus(1L, request, 10L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_STATUS_INVALID.getMessage());
    }

    @DisplayName("이미 삭제된 상품은 상태를 변경할 수 없다")
    @Test
    void should_throw_when_item_already_deleted_on_update_status() throws Exception {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, 1, StoreItemStatus.DELETED, List.of(), List.of("강남"));
        setId(item, 1L);
        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(10L)).thenReturn(java.util.Optional.of(admin()));
        when(redisResourceGuard.tryAcquireWrite(eq(RedisResourceKey.storeItem(1L)), any(Duration.class)))
                .thenReturn(Optional.of(lockHandle));

        StoreItemStatusUpdateRequest request = new StoreItemStatusUpdateRequest(StoreItemStatus.ACTIVE);

        assertThatThrownBy(() -> storeAdminService.updateItemStatus(1L, request, 10L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_ALREADY_DELETED.getMessage());
        verify(lockHandle).close();
    }

    @DisplayName("상품 삭제 시 상태와 삭제 정보가 기록된다")
    @Test
    void should_mark_deleted_on_delete() throws Exception {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, 1, StoreItemStatus.ACTIVE, List.of(), List.of("강남"));
        AdminUser admin = admin();
        setId(admin, 5L);

        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin));
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);
        when(redisResourceGuard.tryAcquireWrite(eq(resourceKey), any(Duration.class)))
                .thenReturn(Optional.of(lockHandle));
        when(redisTransactionCallbackRegistrar.registerCompletion(
                eq(resourceKey),
                any(Runnable.class)
        )).thenReturn(true);

        storeAdminService.deleteItem(1L, 5L);

        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerCompletion(
                eq(resourceKey),
                completionCaptor.capture()
        );
        completionCaptor.getValue().run();
        assertThat(item.getStatus()).isEqualTo(StoreItemStatus.DELETED);
        assertThat(item.getDeletedBy()).isEqualTo(admin);
        assertThat(item.getDeletedAt()).isNotNull();
    }

    @DisplayName("상품 등록 시 엔티티가 저장된다")
    @Test
    void should_create_item_and_save() throws Exception {
        AdminUser admin = admin();
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin));

        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 10, 1, StoreItemStatus.ACTIVE, List.of(), List.of("강남"));
        setId(item, 100L);
        when(storeItemRepository.save(any(StoreItem.class))).thenReturn(item);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(100L);
        when(redisResourceGuard.tryAcquireWrite(eq(resourceKey), any(Duration.class)))
                .thenReturn(Optional.of(lockHandle));
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);

        StoreItemCreateRequest request = new StoreItemCreateRequest(
                "name",
                "desc",
                "cat",
                1000,
                10,
                1,
                StoreItemStatus.ACTIVE,
                List.of(new StoreItemImageRequest("https://cdn.test/main.jpg", 1)),
                List.of("강남", "홍대")
        );

        StoreItemCreateResponse response = storeAdminService.createItem(request, 5L);

        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                actionCaptor.capture(),
                completionCaptor.capture()
        );
        actionCaptor.getValue().run();
        completionCaptor.getValue().run();

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(StoreItemStatus.ACTIVE);
        verify(storeItemRepository).save(any(StoreItem.class));
        verify(storeItemImageRepository).saveAll(any());
        verify(storeStockService).reset(100L, 10);
        verify(lockHandle).close();
    }

    @DisplayName("픽업 장소가 없으면 상품을 등록할 수 없다")
    @Test
    void should_throw_when_pickup_locations_empty_on_create() {
        AdminUser admin = admin();
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin));

        StoreItemCreateRequest request = new StoreItemCreateRequest(
                "name",
                "desc",
                "cat",
                1000,
                10,
                1,
                StoreItemStatus.ACTIVE,
                List.of(new StoreItemImageRequest("https://cdn.test/main.jpg", 1)),
                List.of()
        );

        assertThatThrownBy(() -> storeAdminService.createItem(request, 5L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID.getMessage());
    }

    @DisplayName("상품 수정 시 이미지가 교체된다")
    @Test
    void should_replace_images_on_update() throws Exception {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, 1, StoreItemStatus.ACTIVE, List.of(), List.of("강남"));
        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin()));
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);
        when(redisResourceGuard.tryAcquireWrite(eq(resourceKey), any(Duration.class)))
                .thenReturn(Optional.of(lockHandle));
        when(redisTransactionCallbackRegistrar.registerCompletion(
                eq(resourceKey),
                any(Runnable.class)
        )).thenReturn(true);

        StoreItemUpdateRequest request = new StoreItemUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new StoreItemUpdateRequest.StoreItemImageRequest("https://cdn.test/new.jpg", 1)),
                List.of("강남")
        );

        storeAdminService.updateItem(1L, request, 5L);

        InOrder ordered = inOrder(redisResourceGuard, adminUserRepository, storeItemRepository);
        ordered.verify(redisResourceGuard).tryAcquireWrite(eq(resourceKey), any(Duration.class));
        ordered.verify(adminUserRepository).findById(5L);
        ordered.verify(storeItemRepository).findById(1L);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerCompletion(
                eq(resourceKey),
                completionCaptor.capture()
        );
        completionCaptor.getValue().run();
        verify(storeItemImageRepository).deleteByStoreItem(item);
        verify(storeItemImageRepository).saveAll(any());
        assertThat(item.getImages()).hasSize(1);
    }

    @DisplayName("관리자 재고 수정은 exclusive lock 안에서 commit 후 Redis에 반영")
    @Test
    void should_reset_redis_after_stock_update_commits() throws Exception {
        StoreItem item = StoreItem.create(
                "name",
                "desc",
                "cat",
                1000,
                3,
                1,
                StoreItemStatus.ACTIVE,
                List.of(),
                List.of("강남")
        );
        setId(item, 1L);
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(1L);
        when(adminUserRepository.findById(5L)).thenReturn(Optional.of(admin()));
        when(redisResourceGuard.tryAcquireWrite(eq(resourceKey), any(Duration.class)))
                .thenReturn(Optional.of(lockHandle));
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);
        StoreItemUpdateRequest request = new StoreItemUpdateRequest(
                null,
                null,
                null,
                null,
                8,
                null,
                null,
                null,
                null
        );
        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);

        storeAdminService.updateItem(1L, request, 5L);

        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                actionCaptor.capture(),
                completionCaptor.capture()
        );
        actionCaptor.getValue().run();
        completionCaptor.getValue().run();
        assertThat(item.getStock()).isEqualTo(8);
        verify(storeStockService).reset(1L, 8);
        verify(lockHandle).close();
    }

    @DisplayName("관리자 상품 목록 조회 시 정렬이 적용된다")
    @Test
    void should_apply_sort_when_getting_items() {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, 1, StoreItemStatus.ACTIVE, List.of(), List.of("강남"));
        setId(item, 100L);
        Page<StoreItem> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);
        when(storeItemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(storeItemImageRepository.findThumbnailsByStoreItemIds(anyList())).thenReturn(List.of());

        StoreItemListResponse response = storeAdminService.getItems("ACTIVE", null, null, null, "TITLE_ASC", 0, 10);

        org.mockito.ArgumentCaptor<Pageable> pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(storeItemRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Order.asc("name"), Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        assertThat(response.items()).hasSize(1);
    }

    @DisplayName("알 수 없는 정렬값이면 예외가 발생한다")
    @Test
    void should_throw_when_sort_invalid_on_get_items() {
        assertThatThrownBy(() -> storeAdminService.getItems(null, null, null, null, "UNKNOWN", 0, 10))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_QUERY_INVALID.getMessage());
    }

    @DisplayName("관리자 주문 목록 조회 시 필터와 정렬이 적용된다")
    @Test
    void should_get_admin_orders_with_filters() {
        User user = user("user@test.com");
        setId(user, 11L);
        StoreItem item = StoreItem.create("어드민 굿즈", "설명", "카테고리", 2000, 5, 1, StoreItemStatus.ACTIVE, List.of(), List.of("서울"));
        setId(item, 77L);
        StoreOrder order = StoreOrder.create(user, item, 2000, 2, "서울");
        setId(order, 501L);

        Page<StoreOrder> page = new PageImpl<>(List.of(order), PageRequest.of(0, 20), 1);
        when(storeOrderRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        StoreAdminOrderListResponse response = storeAdminService.getOrders("PURCHASED", "어드민", "USER_EMAIL", "OLDEST", 0, 20);

        assertThat(response.orders()).hasSize(1);
        assertThat(response.orders().get(0).orderId()).isEqualTo(501L);
        assertThat(response.orders().get(0).itemId()).isEqualTo(77L);
        assertThat(response.orders().get(0).userId()).isEqualTo(11L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(storeOrderRepository).findAll(any(Specification.class), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort())
                .isEqualTo(Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
    }

    @DisplayName("관리자 주문 취소 시 재고와 크레딧을 롤백하고 이력을 남긴다")
    @Test
    void should_cancel_admin_order_and_rollback_resources() {
        AdminUser admin = admin();
        setId(admin, 9L);
        when(adminUserRepository.findById(9L)).thenReturn(java.util.Optional.of(admin));

        StoreItem item = StoreItem.create("어드민 굿즈", "설명", "카테고리", 2000, 1, 1, StoreItemStatus.ACTIVE, List.of(), List.of("서울"));
        setId(item, 77L);
        User user = user("user@test.com");
        setId(user, 33L);
        StoreOrder order = StoreOrder.create(user, item, 1500, 2, "서울");
        setId(order, 1000L);
        when(storeOrderRepository.findById(1000L)).thenReturn(java.util.Optional.of(order));
        when(storeOrderRepository.findItemIdById(1000L)).thenReturn(Optional.of(77L));
        RedisResourceKey resourceKey = RedisResourceKey.storeItem(77L);
        when(redisResourceGuard.acquireRead(resourceKey)).thenReturn(lockHandle);
        when(redisTransactionCallbackRegistrar.registerAfterCommit(
                eq(resourceKey),
                any(Runnable.class),
                any(Runnable.class)
        )).thenReturn(true);

        StoreAdminOrderCancelResponse response = storeAdminService.cancelOrder(1000L, 9L);

        ArgumentCaptor<Runnable> actionCaptor = ArgumentCaptor.forClass(Runnable.class);
        ArgumentCaptor<Runnable> completionCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(redisTransactionCallbackRegistrar).registerAfterCommit(
                eq(resourceKey),
                actionCaptor.capture(),
                completionCaptor.capture()
        );
        actionCaptor.getValue().run();
        completionCaptor.getValue().run();
        assertThat(response.orderId()).isEqualTo(1000L);
        assertThat(response.status()).isEqualTo(StoreOrderStatus.CANCELED);
        assertThat(response.refundedAmount()).isEqualTo(3000);
        assertThat(item.getStock()).isEqualTo(3); // 기존 재고 1 + 취소 수량 2
        assertThat(user.getCreditBalance()).isEqualTo(3000);
        verify(creditHistoryRepository).save(any());
        verify(storeStockService).release(77L, 2);
        verify(lockHandle).close();
    }

    private AdminUser admin() {
        return AdminUser.createApproved("admin@test.com", "encoded", "admin", AdminRole.ADMIN);
    }

    private User user(String email) {
        return User.create(email, "사용자", null);
    }

    private void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
