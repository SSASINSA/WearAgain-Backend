package com.ssasinsa.wearagain.domain.store.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemCreateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemStatusUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.request.StoreItemUpdateRequest;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemCreateResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemDetailResponse.StoreItemImageResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemListResponse;
import com.ssasinsa.wearagain.domain.store.dto.response.StoreItemSummaryResponse;
import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemImage;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.exception.StoreErrorCode;
import com.ssasinsa.wearagain.domain.store.exception.StoreException;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemImageRepository;
import com.ssasinsa.wearagain.domain.store.repository.StoreItemRepository;
import jakarta.persistence.criteria.Predicate;
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
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt", "id");

    private final StoreItemRepository storeItemRepository;
    private final StoreItemImageRepository storeItemImageRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public StoreItemCreateResponse createItem(StoreItemCreateRequest request, Long adminId) {
        AdminUser adminUser = getAdmin(adminId);

        StoreItem item = StoreItem.create(
                request.name().trim(),
                normalizeText(request.description()),
                normalizeText(request.category()),
                request.price(),
                request.stock(),
                request.maxPurchasePerUser(),
                request.status() == null ? StoreItemStatus.ACTIVE : request.status(),
                List.of()
        );

        List<SimpleImageRequest> imageRequests = mapCreateImageRequests(request.images());
        List<StoreItemImage> images = buildImages(item, imageRequests);

        StoreItem saved = storeItemRepository.save(item);
        saveImages(saved, images);
        return StoreItemCreateResponse.of(saved.getId(), saved.getName(), saved.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreItemListResponse getItems(String status, String category, String keyword, int page, int size) {
        validatePage(page, size);
        List<StoreItemStatus> statuses = resolveStatuses(status);
        Pageable pageable = PageRequest.of(page, size, DEFAULT_SORT);
        Specification<StoreItem> spec = buildSpecification(statuses, category, keyword);

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

        item.updateInformation(
                normalizeText(request.name()),
                normalizeText(request.description()),
                normalizeText(request.category()),
                request.price(),
                request.stock(),
                request.maxPurchasePerUser()
        );

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

    private Specification<StoreItem> buildSpecification(List<StoreItemStatus> statuses, String category, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!CollectionUtils.isEmpty(statuses)) {
                predicates.add(root.get("status").in(statuses));
            }
            if (StringUtils.hasText(category)) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
            }
            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern));
            }
            query.distinct(true);
            return cb.and(predicates.toArray(Predicate[]::new));
        };
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
                toOffset(item.getCreatedAt()),
                toOffset(item.getUpdatedAt())
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
