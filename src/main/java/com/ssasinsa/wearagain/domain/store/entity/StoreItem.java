package com.ssasinsa.wearagain.domain.store.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.admin.entity.Admin;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "store_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_items_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreItemStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private Admin deletedBy;

    @Builder(access = AccessLevel.PRIVATE)
    private StoreItem(String name, String description, String thumbnailUrl, String category, int price, Integer stock, StoreItemStatus status) {
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.category = category;
        this.price = price;
        this.stock = stock == null ? 0 : stock;
        this.status = status == null ? StoreItemStatus.ACTIVE : status;
    }

    public static StoreItem create(String name, String description, String thumbnailUrl, String category, int price, Integer stock, StoreItemStatus status) {
        return StoreItem.builder()
                .name(name)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .category(category)
                .price(price)
                .stock(stock)
                .status(status)
                .build();
    }

    public void markDeleted(LocalDateTime deletedAt, Admin admin) {
        this.status = StoreItemStatus.DELETED;
        this.deletedAt = deletedAt;
        this.deletedBy = admin;
    }

    public void changeStatus(StoreItemStatus status) {
        this.status = status;
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
