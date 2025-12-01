package com.ssasinsa.wearagain.domain.auth.repository;

import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequest;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestKeywordScope;
import com.ssasinsa.wearagain.domain.auth.entity.AdminSignupRequestStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AdminSignupRequestSpecifications {

    private AdminSignupRequestSpecifications() {
    }

    public static Specification<AdminSignupRequest> statusEquals(AdminSignupRequestStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<AdminSignupRequest> keywordMatches(String keyword, AdminSignupRequestKeywordScope scope) {
        if (!StringUtils.hasText(keyword) || scope == null) {
            return null;
        }
        return (root, query, builder) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            if (scope.includesEmail()) {
                predicates.add(builder.like(builder.lower(root.get("email")), pattern));
            }
            if (scope.includesName()) {
                predicates.add(builder.like(builder.lower(root.get("name")), pattern));
            }
            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            return builder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
