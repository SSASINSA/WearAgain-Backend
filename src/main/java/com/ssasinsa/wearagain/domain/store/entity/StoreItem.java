package com.ssasinsa.wearagain.domain.store.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Entity
@Table(name = "store_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class StoreItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_items_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    @Builder.Default
    private int stock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StoreItemStatus status = StoreItemStatus.ACTIVE;

    @Column(name = "max_purchase_per_user")
    private Integer maxPurchasePerUser;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private AdminUser deletedBy;

    @OneToMany(mappedBy = "storeItem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoreItemImage> images = new ArrayList<>();

    public static StoreItem create(String name, String description, String category, int price, Integer stock, Integer maxPurchasePerUser, StoreItemStatus status, List<String> imageUrls) {
        int resolvedStock = stock == null ? 0 : stock;
        StoreItemStatus resolvedStatus = status == null ? StoreItemStatus.ACTIVE : status;
        Integer resolvedMaxPurchasePerUser = maxPurchasePerUser;
        if (price < 0) {
            throw new IllegalArgumentException("price must be zero or positive");
        }
        if (resolvedStock < 0) {
            throw new IllegalArgumentException("stock must be zero or positive");
        }
        if (resolvedMaxPurchasePerUser != null && resolvedMaxPurchasePerUser <= 0) {
            throw new IllegalArgumentException("maxPurchasePerUser must be positive");
        }

        StoreItem item = StoreItem.builder()
                .name(name)
                .description(description)
                .category(category)
                .price(price)
                .stock(resolvedStock)
                .status(resolvedStatus)
                .maxPurchasePerUser(resolvedMaxPurchasePerUser)
                .build();

        if (imageUrls != null) {
            int order = 0;
            for (String imageUrl : imageUrls) {
                StoreItemImage.create(item, imageUrl, order++);
            }
        }

        return item;
    }

    public void markDeleted(LocalDateTime deletedAt, AdminUser admin) {
        this.status = StoreItemStatus.DELETED;
        this.deletedAt = deletedAt;
        this.deletedBy = admin;
    }

    public void updateInformation(String name, String description, String category, Integer price, Integer stock, Integer maxPurchasePerUser) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (category != null) {
            this.category = category;
        }
        if (price != null) {
            this.price = price;
        }
        if (stock != null) {
            this.stock = stock;
        }
        if (maxPurchasePerUser != null) {
            if (maxPurchasePerUser <= 0) {
                throw new IllegalArgumentException("maxPurchasePerUser must be positive");
            }
            this.maxPurchasePerUser = maxPurchasePerUser;
        }
    }

    public void changeStatus(StoreItemStatus status) {
        this.status = status;
    }

    public void replaceImages(List<StoreItemImage> newImages) {
        images.clear();
        if (newImages != null) {
            images.addAll(newImages);
        }
    }

    public void decreaseStock(int quantity) {
        validateQuantity(quantity);
        if (this.stock < quantity) {
            throw new IllegalStateException("insufficient stock");
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        validateQuantity(quantity);
        this.stock += quantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    void addImage(StoreItemImage image) {
        images.add(image);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StoreItem)) {
            return false;
        }
        StoreItem other = (StoreItem) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
