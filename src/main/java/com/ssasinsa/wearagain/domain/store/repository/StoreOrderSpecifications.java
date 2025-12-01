package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreOrder;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderKeywordScope;
import com.ssasinsa.wearagain.domain.store.entity.StoreOrderStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class StoreOrderSpecifications {

    private StoreOrderSpecifications() {
    }

    public static Specification<StoreOrder> statusEquals(StoreOrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<StoreOrder> keywordMatches(String keyword, StoreOrderKeywordScope scope) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword) || scope == null) {
                return cb.conjunction();
            }
            var lowered = "%" + keyword.toLowerCase() + "%";
            boolean searchEmail = scope == StoreOrderKeywordScope.ALL || scope == StoreOrderKeywordScope.USER_EMAIL;
            boolean searchItemName = scope == StoreOrderKeywordScope.ALL || scope == StoreOrderKeywordScope.ITEM_NAME;
            if (!searchEmail && !searchItemName) {
                return cb.conjunction();
            }

            Predicate predicate = cb.disjunction();
            if (searchEmail) {
                var userJoin = root.join("user", JoinType.LEFT);
                predicate = cb.or(predicate, cb.like(cb.lower(userJoin.get("email")), lowered));
            }

            if (searchItemName) {
                var itemJoin = root.join("item", JoinType.LEFT);
                predicate = cb.or(predicate, cb.like(cb.lower(itemJoin.get("name")), lowered));
            }

            return predicate;
        };
    }
}
