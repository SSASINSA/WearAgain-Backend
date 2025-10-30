package com.ssasinsa.wearagain.domain.finance.entity;

import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "credit_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "created_at", insertable = false, updatable = false))
})
public class CreditHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "credit_histories_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_store_orders_id")
    private StoreOrder relatedOrder;

    @Column(name = "change_amount", nullable = false)
    private int changeAmount;

    @Column(length = 255)
    private String reason;

    @Builder(access = AccessLevel.PRIVATE)
    private CreditHistory(User user, StoreOrder relatedOrder, int changeAmount, String reason) {
        this.user = user;
        this.relatedOrder = relatedOrder;
        this.changeAmount = changeAmount;
        this.reason = reason;
    }

    public static CreditHistory create(User user, StoreOrder relatedOrder, int changeAmount, String reason) {
        return CreditHistory.builder()
                .user(user)
                .relatedOrder(relatedOrder)
                .changeAmount(changeAmount)
                .reason(reason)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreditHistory)) {
            return false;
        }
        CreditHistory other = (CreditHistory) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
