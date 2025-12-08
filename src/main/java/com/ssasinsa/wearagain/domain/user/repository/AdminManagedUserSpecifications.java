package com.ssasinsa.wearagain.domain.user.repository;

import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserKeywordScope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class AdminManagedUserSpecifications {

    private AdminManagedUserSpecifications() {
    }

    public static Specification<AdminUser> statusEquals(AdminStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<AdminUser> keywordMatches(String keyword, AdminManagedUserKeywordScope scope) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> {
            switch (scope) {
                case EMAIL -> {
                    return cb.like(cb.lower(root.get("email")), pattern);
                }
                case NAME -> {
                    return cb.like(cb.lower(root.get("name")), pattern);
                }
                case ALL -> {
                    return cb.or(
                            cb.like(cb.lower(root.get("email")), pattern),
                            cb.like(cb.lower(root.get("name")), pattern)
                    );
                }
                default -> {
                    return cb.or(
                            cb.like(cb.lower(root.get("email")), pattern),
                            cb.like(cb.lower(root.get("name")), pattern)
                    );
                }
            }
        };
    }
}
