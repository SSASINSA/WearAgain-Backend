package com.ssasinsa.wearagain.domain.store.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "store_item_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class StoreItemImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_item_images_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_items_id", nullable = false)
    private StoreItem storeItem;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public static StoreItemImage create(StoreItem storeItem, String imageUrl, int sortOrder) {
        StoreItemImage image = StoreItemImage.builder()
                .storeItem(storeItem)
                .imageUrl(imageUrl)
                .sortOrder(sortOrder)
                .build();
        if (storeItem != null) {
            storeItem.addImage(image);
        }
        return image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StoreItemImage)) {
            return false;
        }
        StoreItemImage other = (StoreItemImage) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
