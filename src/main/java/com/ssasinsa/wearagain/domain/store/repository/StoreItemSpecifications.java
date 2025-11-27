package com.ssasinsa.wearagain.domain.store.repository;

import com.ssasinsa.wearagain.domain.store.entity.StoreItem;
import com.ssasinsa.wearagain.domain.store.entity.StoreItemStatus;
import com.ssasinsa.wearagain.domain.store.entity.StoreKeywordScope;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class StoreItemSpecifications {

    private StoreItemSpecifications() {
    }

    public static Specification<StoreItem> statusIn(Collection<StoreItemStatus> statuses) {
        return (root, query, builder) -> root.get("status").in(statuses);
    }

    public static Specification<StoreItem> categoryEquals(String category) {
        if (!StringUtils.hasText(category)) {
            return (root, query, builder) -> builder.conjunction();
        }
        String normalized = category.trim().toLowerCase();
        return (root, query, builder) -> builder.equal(builder.lower(root.get("category")), normalized);
    }

    public static Specification<StoreItem> keywordMatches(String keyword, StoreKeywordScope scope) {
        if (!StringUtils.hasText(keyword) || scope == null) {
            return (root, query, builder) -> builder.conjunction();
        }
        String pattern = "%" + escapeLike(keyword.toLowerCase()) + "%";

        boolean searchName = scope == StoreKeywordScope.ALL || scope == StoreKeywordScope.NAME;
        boolean searchDescription = scope == StoreKeywordScope.ALL || scope == StoreKeywordScope.DESCRIPTION;
        boolean searchCategory = scope == StoreKeywordScope.ALL || scope == StoreKeywordScope.CATEGORY;

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchName) {
                predicates.add(builder.like(builder.lower(root.get("name")), pattern, '\\'));
            }
            if (searchDescription) {
                predicates.add(builder.like(builder.lower(root.get("description")), pattern, '\\'));
            }
            if (searchCategory) {
                predicates.add(builder.like(builder.lower(root.get("category")), pattern, '\\'));
            }
            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            query.distinct(true);
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLike(String keyword) {
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
