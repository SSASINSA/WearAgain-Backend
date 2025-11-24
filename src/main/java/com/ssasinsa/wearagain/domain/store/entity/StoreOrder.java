package com.ssasinsa.wearagain.domain.store.entity;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "store_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "purchased_at", updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "canceled_at"))
})
public class StoreOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_orders_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_items_id", nullable = false)
    private StoreItem item;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StoreOrderStatus status;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private StoreOrder(User user, StoreItem item, StoreOrderStatus status, int price, int quantity) {
        validatePrice(price);
        validateQuantity(quantity);
        this.user = user;
        this.item = item;
        this.status = status == null ? StoreOrderStatus.PURCHASED : status;
        this.price = price;
        this.quantity = quantity;
    }

    public static StoreOrder create(User user, StoreItem item, int price, int quantity) {
        return StoreOrder.builder()
                .user(user)
                .item(item)
                .price(price)
                .quantity(quantity)
                .build();
    }

    public void cancel() {
        this.status = StoreOrderStatus.CANCELED;
    }

    private void validatePrice(int price) {
        if (price < 0) {
            throw new IllegalArgumentException("price must be zero or positive");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    public void markFailed() {
        this.status = StoreOrderStatus.FAILED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StoreOrder)) {
            return false;
        }
        StoreOrder other = (StoreOrder) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
