package com.ssasinsa.wearagain.domain.store.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private AdminUser deletedBy;

    @OneToMany(mappedBy = "storeItem", fetch = FetchType.LAZY)
    @Builder.Default
    private List<StoreItemImage> images = new ArrayList<>();

    public static StoreItem create(String name, String description, String category, int price, Integer stock, StoreItemStatus status, List<String> imageUrls) {
        int resolvedStock = stock == null ? 0 : stock;
        StoreItemStatus resolvedStatus = status == null ? StoreItemStatus.ACTIVE : status;

        StoreItem item = StoreItem.builder()
                .name(name)
                .description(description)
                .category(category)
                .price(price)
                .stock(resolvedStock)
                .status(resolvedStatus)
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

    public void changeStatus(StoreItemStatus status) {
        this.status = status;
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
