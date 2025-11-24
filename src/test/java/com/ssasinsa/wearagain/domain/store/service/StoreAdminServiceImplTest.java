package com.ssasinsa.wearagain.domain.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest.StoreItemImageRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StoreAdminServiceImplTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private StoreItemImageRepository storeItemImageRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @InjectMocks
    private StoreAdminServiceImpl storeAdminService;

    @DisplayName("상품 상태 변경 시 상태가 null이면 예외 발생")
    @Test
    void should_throw_when_status_null_on_update_status() {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, StoreItemStatus.ACTIVE, List.of());
        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(10L)).thenReturn(java.util.Optional.of(admin()));

        StoreItemStatusUpdateRequest request = new StoreItemStatusUpdateRequest(null);

        assertThatThrownBy(() -> storeAdminService.updateItemStatus(1L, request, 10L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_STATUS_INVALID.getMessage());
    }

    @DisplayName("삭제된 상품은 상태 변경 불가")
    @Test
    void should_throw_when_item_already_deleted_on_update_status() {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, StoreItemStatus.DELETED, List.of());
        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(10L)).thenReturn(java.util.Optional.of(admin()));

        StoreItemStatusUpdateRequest request = new StoreItemStatusUpdateRequest(StoreItemStatus.ACTIVE);

        assertThatThrownBy(() -> storeAdminService.updateItemStatus(1L, request, 10L))
                .isInstanceOf(StoreException.class)
                .hasMessage(StoreErrorCode.STORE_ITEM_ALREADY_DELETED.getMessage());
    }

    @DisplayName("상품 삭제 시 상태와 삭제자 정보가 설정된다")
    @Test
    void should_mark_deleted_on_delete() {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, StoreItemStatus.ACTIVE, List.of());
        AdminUser admin = admin();
        setId(admin, 5L);

        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin));

        storeAdminService.deleteItem(1L, 5L);

        assertThat(item.getStatus()).isEqualTo(StoreItemStatus.DELETED);
        assertThat(item.getDeletedBy()).isEqualTo(admin);
        assertThat(item.getDeletedAt()).isNotNull();
    }

    @DisplayName("상품 등록 시 리포지토리에 저장된다")
    @Test
    void should_create_item_and_save() {
        AdminUser admin = admin();
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin));

        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 10, StoreItemStatus.ACTIVE, List.of());
        setId(item, 100L);
        when(storeItemRepository.save(any(StoreItem.class))).thenReturn(item);

        StoreItemCreateRequest request = new StoreItemCreateRequest(
                "name",
                "desc",
                "cat",
                1000,
                10,
                StoreItemStatus.ACTIVE,
                List.of(new StoreItemImageRequest("https://cdn.test/main.jpg", 1))
        );

        StoreItemCreateResponse response = storeAdminService.createItem(request, 5L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(StoreItemStatus.ACTIVE);
        verify(storeItemRepository).save(any(StoreItem.class));
        verify(storeItemImageRepository).saveAll(any());
    }

    @DisplayName("이미지 교체 요청 시 기존 이미지를 삭제하고 새 이미지를 저장한다")
    @Test
    void should_replace_images_on_update() {
        StoreItem item = StoreItem.create("name", "desc", "cat", 1000, 0, StoreItemStatus.ACTIVE, List.of());
        when(storeItemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(adminUserRepository.findById(5L)).thenReturn(java.util.Optional.of(admin()));

        StoreItemUpdateRequest request = new StoreItemUpdateRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new StoreItemUpdateRequest.StoreItemImageRequest("https://cdn.test/new.jpg", 1))
        );

        storeAdminService.updateItem(1L, request, 5L);

        verify(storeItemImageRepository).deleteByStoreItem(item);
        verify(storeItemImageRepository).saveAll(any());
        assertThat(item.getImages()).hasSize(1);
    }

    private AdminUser admin() {
        return AdminUser.createApproved("admin@test.com", "encoded", "admin", AdminRole.ADMIN);
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
