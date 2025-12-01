package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreAdminOrderCancelResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreAdminOrderListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreAdminOrderSummaryResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse.StoreItemImageResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemSummaryResponse;
import com.ssasinsa.wearagain.domain.store.entity.StoreAdminItemSortType;
import com.ssasinsa.wearagain.domain.store.entity.StoreAdminOrderSortType;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemImage;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.entity.StoreKeywordScope;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderKeywordScope;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemSpecifications;
import com.ssasinsa.wearagain.domain.store.repository.StoreOrderRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreOrderSpecifications;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class StoreAdminServiceImpl implements StoreAdminService {

    private static final int MAX_IMAGE_COUNT = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final StoreItemRepository storeItemRepository;
    private final StoreItemImageRepository storeItemImageRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public StoreItemCreateResponse createItem(StoreItemCreateRequest request, Long adminId) {
        AdminUser adminUser = getAdmin(adminId);

        StoreItem item;
        try {
            item = StoreItem.create(
                    request.name().trim(),
                    normalizeText(request.description()),
                    normalizeText(request.category()),
                    request.price(),
                    request.stock(),
                    request.maxPurchasePerUser(),
                    request.status() == null ? StoreItemStatus.ACTIVE : request.status(),
                    List.of(),
                    request.pickupLocations()
            );
        } catch (IllegalArgumentException exception) {
            throw new StoreException(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID, exception);
        }

        List<SimpleImageRequest> imageRequests = mapCreateImageRequests(request.images());
        List<StoreItemImage> images = buildImages(item, imageRequests);

        StoreItem saved = storeItemRepository.save(item);
        saveImages(saved, images);
        return StoreItemCreateResponse.of(saved.getId(), saved.getName(), saved.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemListResponse getItems(String status, String category, String keyword, String keywordScope, String sort, int page, int size) {
        validatePage(page, size);
        List<StoreItemStatus> statuses = resolveStatuses(status);
        Pageable pageable = PageRequest.of(page, size, resolveSort(sort));
        String normalizedCategory = normalizeText(category);
        String normalizedKeyword = normalizeText(keyword);
        StoreKeywordScope scope = resolveKeywordScope(keywordScope);
        Specification<StoreItem> spec = StoreItemSpecifications.statusIn(statuses)
                .and(StoreItemSpecifications.categoryEquals(normalizedCategory))
                .and(StoreItemSpecifications.keywordMatches(normalizedKeyword, scope));

        Page<StoreItem> result = storeItemRepository.findAll(spec, pageable);
        List<StoreItem> storeItems = result.getContent();
        Map<Long, String> thumbnails = loadThumbnails(storeItems);
        List<StoreItemSummaryResponse> items = storeItems.stream()
                .map(item -> mapToSummary(item, thumbnails.get(item.getId())))
                .toList();

        return new StoreItemListResponse(
                items,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemDetailResponse getItemDetail(Long itemId) {
        StoreItem item = findItem(itemId);
        return mapToDetail(item);
    }

    @Override
    @Transactional
    public StoreItemDetailResponse updateItem(Long itemId, StoreItemUpdateRequest request, Long adminId) {
        getAdmin(adminId);
        StoreItem item = findItem(itemId);
        ensureNotDeleted(item);

        try {
            item.updateInformation(
                    normalizeText(request.name()),
                    normalizeText(request.description()),
                    normalizeText(request.category()),
                    request.price(),
                    request.stock(),
                    request.maxPurchasePerUser(),
                    request.pickupLocations()
            );
        } catch (IllegalArgumentException exception) {
            throw new StoreException(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID, exception);
        }

        if (request.status() != null) {
            item.changeStatus(request.status());
        }

        if (request.images() != null) {
            List<SimpleImageRequest> imageRequests = mapUpdateImageRequests(request.images());
            List<StoreItemImage> images = buildImages(item, imageRequests);
            storeItemImageRepository.deleteByStoreItem(item);
            item.replaceImages(images);
            saveImages(item, images);
        }

        return mapToDetail(item);
    }

    @Override
    @Transactional
    public StoreItemDetailResponse updateItemStatus(Long itemId, StoreItemStatusUpdateRequest request, Long adminId) {
        getAdmin(adminId);
        StoreItem item = findItem(itemId);
        ensureNotDeleted(item);
        if (request.status() == null) {
            throw new StoreException(StoreErrorCode.STORE_ITEM_STATUS_INVALID);
        }
        item.changeStatus(request.status());
        return mapToDetail(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId, Long adminId) {
        AdminUser adminUser = getAdmin(adminId);
        StoreItem item = findItem(itemId);
        ensureNotDeleted(item);
        item.markDeleted(LocalDateTime.now(ZoneOffset.UTC), adminUser);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreAdminOrderListResponse getOrders(String status, String keyword, String keywordScope, String sort, int page, int size) {
        validatePage(page, size);
        StoreOrderStatus statusFilter = resolveOrderStatus(status);
        StoreOrderKeywordScope keywordScopeFilter = resolveOrderKeywordScope(keywordScope);
        Pageable pageable = PageRequest.of(page, size, resolveOrderSort(sort));
        String normalizedKeyword = normalizeText(keyword);

        Specification<StoreOrder> spec = StoreOrderSpecifications.statusEquals(statusFilter)
                .and(StoreOrderSpecifications.keywordMatches(normalizedKeyword, keywordScopeFilter));

        Page<StoreOrder> result = storeOrderRepository.findAll(spec, pageable);
        List<StoreAdminOrderSummaryResponse> orders = result.getContent().stream()
                .map(this::mapToAdminOrderSummary)
                .toList();

        return new StoreAdminOrderListResponse(
                orders,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    @Override
    @Transactional
    public StoreAdminOrderCancelResponse cancelOrder(Long orderId, Long adminId) {
        getAdmin(adminId);
        StoreOrder order = findOrder(orderId);
        if (order.getStatus() != StoreOrderStatus.PURCHASED) {
            throw new StoreException(StoreErrorCode.STORE_ORDER_CANCEL_INVALID);
        }

        StoreItem item = order.getItem();
        item.increaseStock(order.getQuantity());

        User user = order.getUser();
        int refundAmount = order.getPrice() * order.getQuantity();
        user.increaseCreditBalance(refundAmount);
        order.cancel();

        creditHistoryRepository.save(CreditHistory.create(user, order, refundAmount, "STORE_CANCEL_ADMIN"));

        return new StoreAdminOrderCancelResponse(
                order.getId(),
                order.getStatus(),
                refundAmount,
                toOffset(order.getUpdatedAt())
        );
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > MAX_PAGE_SIZE) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID);
        }
    }

    private AdminUser getAdmin(Long adminId) {
        if (adminId == null) {
            throw new StoreException(StoreErrorCode.STORE_UNAUTHORIZED);
        }
        return adminUserRepository.findById(adminId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_FORBIDDEN));
    }

    private StoreItem findItem(Long itemId) {
        return storeItemRepository.findById(itemId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_ITEM_NOT_FOUND));
    }

    private StoreOrder findOrder(Long orderId) {
        return storeOrderRepository.findById(orderId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_ORDER_NOT_FOUND));
    }

    private void ensureNotDeleted(StoreItem item) {
        if (item.getStatus() == StoreItemStatus.DELETED) {
            throw new StoreException(StoreErrorCode.STORE_ITEM_ALREADY_DELETED);
        }
    }

    private List<SimpleImageRequest> mapCreateImageRequests(List<StoreItemCreateRequest.StoreItemImageRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        return requests.stream()
                .map(req -> new SimpleImageRequest(req.imageUrl(), req.sortOrder()))
                .toList();
    }

    private List<SimpleImageRequest> mapUpdateImageRequests(List<StoreItemUpdateRequest.StoreItemImageRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        return requests.stream()
                .map(req -> new SimpleImageRequest(req.imageUrl(), req.sortOrder()))
                .toList();
    }

    private List<StoreItemImage> buildImages(StoreItem item, List<SimpleImageRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            return List.of();
        }
        if (requests.size() > MAX_IMAGE_COUNT) {
            throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
        }
        Set<Integer> orders = new HashSet<>();
        List<StoreItemImage> images = new ArrayList<>();
        for (SimpleImageRequest request : requests) {
            if (request.sortOrder == null || request.sortOrder <= 0 || !orders.add(request.sortOrder)) {
                throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
            }
            String imageUrl = normalizeText(request.imageUrl);
            if (!StringUtils.hasText(imageUrl)) {
                throw new StoreException(StoreErrorCode.STORE_IMAGE_INVALID);
            }
            StoreItemImage image = StoreItemImage.create(item, imageUrl, request.sortOrder);
            images.add(image);
        }
        return images;
    }

    private List<StoreItemStatus> resolveStatuses(String param) {
        if (!StringUtils.hasText(param)) {
            return List.of(StoreItemStatus.ACTIVE, StoreItemStatus.INACTIVE, StoreItemStatus.DELETED);
        }
        List<StoreItemStatus> statuses = new ArrayList<>();
        for (String token : param.split(",")) {
            if (!StringUtils.hasText(token)) {
                continue;
            }
            try {
                statuses.add(StoreItemStatus.valueOf(token.trim().toUpperCase()));
            } catch (IllegalArgumentException exception) {
                throw new StoreException(StoreErrorCode.STORE_ITEM_STATUS_INVALID, exception);
            }
        }
        if (statuses.isEmpty()) {
            throw new StoreException(StoreErrorCode.STORE_ITEM_STATUS_INVALID);
        }
        return statuses;
    }

    private StoreOrderStatus resolveOrderStatus(String param) {
        if (!StringUtils.hasText(param)) {
            return null;
        }
        try {
            return StoreOrderStatus.valueOf(param.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StoreException(StoreErrorCode.STORE_ORDER_STATUS_INVALID, exception);
        }
    }

    private StoreOrderKeywordScope resolveOrderKeywordScope(String param) {
        if (!StringUtils.hasText(param)) {
            return StoreOrderKeywordScope.ALL;
        }
        try {
            return StoreOrderKeywordScope.valueOf(param.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID, exception);
        }
    }

    private StoreKeywordScope resolveKeywordScope(String param) {
        if (!StringUtils.hasText(param)) {
            return StoreKeywordScope.ALL;
        }
        try {
            return StoreKeywordScope.valueOf(param.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID, exception);
        }
    }

    private Sort resolveSort(String param) {
        StoreAdminItemSortType sortType;
        if (!StringUtils.hasText(param)) {
            sortType = StoreAdminItemSortType.LATEST;
        } else {
            try {
                sortType = StoreAdminItemSortType.valueOf(param.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID, exception);
            }
        }
        return sortType.toSort();
    }

    private Sort resolveOrderSort(String param) {
        StoreAdminOrderSortType sortType;
        if (!StringUtils.hasText(param)) {
            sortType = StoreAdminOrderSortType.LATEST;
        } else {
            try {
                sortType = StoreAdminOrderSortType.valueOf(param.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID, exception);
            }
        }
        return sortType.toSort();
    }

    private Map<Long, String> loadThumbnails(List<StoreItem> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Map.of();
        }
        List<Long> ids = items.stream()
                .map(StoreItem::getId)
                .toList();
        List<StoreItemImage> thumbnailEntities = storeItemImageRepository.findThumbnailsByStoreItemIds(ids);
        Map<Long, String> thumbnails = new HashMap<>();
        for (StoreItemImage image : thumbnailEntities) {
            Long storeItemId = image.getStoreItem() != null ? image.getStoreItem().getId() : null;
            if (storeItemId != null && !thumbnails.containsKey(storeItemId)) {
                thumbnails.put(storeItemId, image.getImageUrl());
            }
        }
        return thumbnails;
    }

    private StoreItemSummaryResponse mapToSummary(StoreItem item, String thumbnailUrl) {
        return new StoreItemSummaryResponse(
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getPrice(),
                item.getStock(),
                item.getMaxPurchasePerUser(),
                item.getStatus(),
                thumbnailUrl,
                toOffset(item.getCreatedAt()),
                toOffset(item.getUpdatedAt())
        );
    }

    private StoreItemDetailResponse mapToDetail(StoreItem item) {
        List<StoreItemImageResponse> images = item.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(image -> new StoreItemImageResponse(
                        image.getId(),
                        image.getImageUrl(),
                        image.getSortOrder()
                ))
                .toList();
        return new StoreItemDetailResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getCategory(),
                item.getPrice(),
                item.getStock(),
                item.getMaxPurchasePerUser(),
                item.getStatus(),
                images,
                List.copyOf(item.getPickupLocations()),
                toOffset(item.getCreatedAt()),
                toOffset(item.getUpdatedAt())
        );
    }

    private StoreAdminOrderSummaryResponse mapToAdminOrderSummary(StoreOrder order) {
        User user = order.getUser();
        StoreItem item = order.getItem();
        int totalPrice = order.getPrice() * order.getQuantity();
        return new StoreAdminOrderSummaryResponse(
                order.getId(),
                user != null ? user.getId() : null,
                user != null ? user.getEmail() : null,
                item != null ? item.getId() : null,
                item != null ? item.getName() : null,
                order.getQuantity(),
                order.getPrice(),
                totalPrice,
                order.getPickupLocation(),
                order.getStatus(),
                toOffset(order.getCreatedAt()),
                order.getStatus() == StoreOrderStatus.CANCELED ? toOffset(order.getUpdatedAt()) : null
        );
    }

    private OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void saveImages(StoreItem item, List<StoreItemImage> images) {
        if (CollectionUtils.isEmpty(images)) {
            return;
        }
        images.forEach(image -> {
            if (image.getStoreItem() == null) {
                StoreItemImage.create(item, image.getImageUrl(), image.getSortOrder());
            }
        });
        storeItemImageRepository.saveAll(images);
    }

    private record SimpleImageRequest(String imageUrl, Integer sortOrder) {
    }
}
