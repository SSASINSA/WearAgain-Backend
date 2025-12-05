package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreOrderCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.*;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse.StoreItemImageResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreOrderDetailResponse.OrderItem;
import com.ssasinsa.wearagain.domain.store.entity.*;
import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreOrderRepository;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private static final int MAX_PAGE_SIZE = 50;

    private final StoreItemRepository storeItemRepository;
    private final StoreItemImageRepository storeItemImageRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final UserRepository userRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final StoreStockService storeStockService;

    @Override
    @Transactional(readOnly = true)
    public StoreItemCursorListResponse getItems(String category, String keyword, String cursor, int size) {
        int pageSize = resolveSize(size);
        Long cursorId = decodeIdCursor(cursor);
        String normalizedCategory = normalizeText(category);
        String normalizedKeyword = normalizeText(keyword);
        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Sort.Direction.DESC, "id"));

        List<StoreItem> content = new ArrayList<>(storeItemRepository.findActiveItemsWithCursor(
                StoreItemStatus.ACTIVE,
                normalizedCategory,
                normalizedKeyword,
                cursorId,
                pageable
        ));
        boolean hasNext = content.size() > pageSize;
        if (hasNext) {
            content = content.subList(0, pageSize);
        }

        Map<Long, String> thumbnails = loadThumbnails(content);
        List<StoreItemSummaryResponse> items = content.stream()
                .map(item -> mapToSummary(item, thumbnails.get(item.getId())))
                .toList();
        String nextCursor = hasNext && !content.isEmpty()
                ? encodeCursor(content.get(content.size() - 1).getId())
                : null;

        return new StoreItemCursorListResponse(items, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemDetailResponse getItemDetail(Long itemId) {
        StoreItem item = findActiveItem(itemId);
        return mapToDetail(item);
    }

    @Override
    @Transactional
    public StoreOrderCreateResponse createOrder(StoreOrderCreateRequest request, Long userId) {
        User user = findUser(userId);
        StoreItem item = findActiveItem(request.itemId());
        validatePickupLocation(item, request.pickupLocation());

        if (request.quantity() <= 0) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID);
        }

        int unitPrice = item.getPrice();
        int usedCredit = unitPrice * request.quantity();

        enforcePurchaseLimit(user, item, request.quantity());

        // 캐시 우선 예약
        boolean reserved = storeStockService.reserve(item.getId(), request.quantity());
        if (!reserved) {
            throw new StoreException(StoreErrorCode.STORE_STOCK_SHORTAGE);
        }

        try {
            user.decreaseCreditBalance(usedCredit);
        } catch (IllegalStateException exception) {
            storeStockService.release(item.getId(), request.quantity());
            throw new StoreException(StoreErrorCode.STORE_CREDIT_NOT_ENOUGH, exception);
        }

        StoreOrder order = StoreOrder.create(user, item, unitPrice, request.quantity(), request.pickupLocation().trim());
        StoreOrder saved = storeOrderRepository.save(order);
        creditHistoryRepository.save(CreditHistory.create(user, saved, -usedCredit, "STORE_PURCHASE"));

        // DB 재고를 캐시 결과에 맞춰 감소
        try {
            item.decreaseStock(request.quantity());
        } catch (RuntimeException exception) {
            storeStockService.release(item.getId(), request.quantity());
            throw exception;
        }

        return new StoreOrderCreateResponse(
                saved.getId(),
                item.getId(),
                item.getName(),
                saved.getQuantity(),
                saved.getPrice(),
                usedCredit,
                saved.getPickupLocation(),
                saved.getStatus(),
                toOffset(saved.getCreatedAt())
        );
    }

    @Override
    @Transactional
    public StoreOrderCancelResponse cancelOrder(Long orderId, Long userId) {
        User user = findUser(userId);
        StoreOrder order = findOrderOwnedBy(orderId, user);
        if (order.getStatus() != StoreOrderStatus.PURCHASED) {
            throw new StoreException(StoreErrorCode.STORE_ORDER_CANCEL_INVALID);
        }

        StoreItem item = order.getItem();

        storeStockService.release(item.getId(), order.getQuantity());
        item.increaseStock(order.getQuantity());
        int refundAmount = order.getPrice() * order.getQuantity();
        user.increaseCreditBalance(refundAmount);
        order.cancel();

        creditHistoryRepository.save(CreditHistory.create(user, order, refundAmount, "STORE_CANCEL"));

        return new StoreOrderCancelResponse(
                order.getId(),
                order.getStatus(),
                refundAmount,
                toOffset(order.getUpdatedAt())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StoreOrderListResponse getOrders(String cursor, int size, StoreOrderStatus status, Long userId) {
        int pageSize = resolveSize(size);
        User user = findUser(userId);
        Long cursorId = decodeIdCursor(cursor);
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        List<StoreOrder> orders = storeOrderRepository.findAllWithCursor(
                user,
                status,
                cursorId,
                pageable
        );

        boolean hasNext = orders.size() > pageSize;
        if (hasNext) {
            orders = orders.subList(0, pageSize);
        }

        List<StoreItem> orderItems = orders.stream()
                .map(StoreOrder::getItem)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, String> thumbnails = loadThumbnails(orderItems);

        List<StoreOrderSummaryResponse> responses = orders.stream()
                .map(order -> mapToOrderSummary(order, thumbnails))
                .toList();

        String nextCursor = hasNext && !orders.isEmpty()
                ? encodeCursor(orders.get(orders.size() - 1).getId())
                : null;

        return new StoreOrderListResponse(responses, nextCursor, hasNext);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreOrderDetailResponse getOrderDetail(Long orderId, Long userId) {
        User user = findUser(userId);
        StoreOrder order = findOrderOwnedBy(orderId, user);
        StoreItem item = order.getItem();
        List<StoreItemImageResponse> images = item.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(img -> new StoreItemImageResponse(img.getId(), img.getImageUrl(), img.getSortOrder()))
                .toList();

        return new StoreOrderDetailResponse(
                order.getId(),
                new OrderItem(item.getId(), item.getName(), item.getPrice(), images),
                order.getQuantity(),
                order.getPrice(),
                order.getPrice() * order.getQuantity(),
                order.getPickupLocation(),
                order.getStatus(),
                toOffset(order.getCreatedAt()),
                order.getStatus() == StoreOrderStatus.CANCELED ? toOffset(order.getUpdatedAt()) : null
        );
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

    private StoreOrderSummaryResponse mapToOrderSummary(StoreOrder order, Map<Long, String> thumbnails) {
        User orderOwner = order.getUser();
        StoreItem item = order.getItem();
        int totalPrice = order.getPrice() * order.getQuantity();
        String thumbnailUrl = thumbnails.get(item.getId());

        return new StoreOrderSummaryResponse(
                order.getId(),
                orderOwner != null ? orderOwner.getEmail() : null,
                item != null ? item.getId() : null,
                item != null ? item.getName() : null,
                thumbnailUrl,
                order.getQuantity(),
                order.getPrice(),
                totalPrice,
                order.getPickupLocation(),
                order.getStatus(),
                toOffset(order.getCreatedAt()),
                order.getStatus() == StoreOrderStatus.CANCELED ? toOffset(order.getUpdatedAt()) : null
        );
    }

    private User findUser(Long userId) {
        if (userId == null) {
            throw new CustomException(CommonErrorCode.UNAUTHORIZED);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));
    }

    private StoreItem findActiveItem(Long itemId) {
        StoreItem item = storeItemRepository.findById(itemId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_ITEM_NOT_FOUND));
        if (item.getStatus() != StoreItemStatus.ACTIVE) {
            throw new StoreException(StoreErrorCode.STORE_ITEM_INACTIVE);
        }
        return item;
    }

    private void validatePickupLocation(StoreItem item, String pickupLocation) {
        if (!item.hasPickupLocation(pickupLocation)) {
            throw new StoreException(StoreErrorCode.STORE_PICKUP_LOCATION_INVALID);
        }
    }

    private void enforcePurchaseLimit(User user, StoreItem item, int quantity) {
        Integer maxPerUser = item.getMaxPurchasePerUser();
        if (maxPerUser == null) {
            return;
        }
        long purchasedCount = storeOrderRepository.countByUserAndItemAndStatus(user, item, StoreOrderStatus.PURCHASED);
        if ((long) quantity + purchasedCount > maxPerUser) {
            throw new StoreException(StoreErrorCode.STORE_PURCHASE_LIMIT_EXCEEDED);
        }
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

    private Long decodeIdCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (Exception exception) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID, exception);
        }
    }

    private String encodeCursor(Long id) {
        return String.valueOf(id);
    }

    private StoreOrder findOrderOwnedBy(Long orderId, User user) {
        return storeOrderRepository.findById(orderId)
                .filter(order -> order.getUser().equals(user))
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_ORDER_NOT_FOUND));
    }

    private OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atOffset(ZoneOffset.UTC);
    }

    private int resolveSize(int size) {
        if (size <= 0) {
            return 20;
        }
        if (size > MAX_PAGE_SIZE) {
            throw new StoreException(StoreErrorCode.STORE_QUERY_INVALID);
        }
        return size;
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
