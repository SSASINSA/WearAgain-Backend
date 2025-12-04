package com.ssasinsa.wearagain.domain.user.repository;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantKeywordScope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> suspendedEquals(Boolean suspended) {
        return (root, query, cb) -> {
            if (suspended == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("suspended"), suspended);
        };
    }

    public static Specification<User> keywordMatches(String keyword, AdminParticipantKeywordScope scope) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword) || scope == null) {
                return cb.conjunction();
            }
            String lowered = "%" + keyword.trim().toLowerCase() + "%";
            boolean searchEmail = scope == AdminParticipantKeywordScope.ALL || scope == AdminParticipantKeywordScope.EMAIL;
            boolean searchName = scope == AdminParticipantKeywordScope.ALL || scope == AdminParticipantKeywordScope.NAME;
            if (!searchEmail && !searchName) {
                return cb.conjunction();
            }
            var loweredEmail = cb.lower(root.get("email"));
            var loweredName = cb.lower(root.get("displayName"));
            if (searchEmail && searchName) {
                return cb.or(
                        cb.like(loweredEmail, lowered),
                        cb.like(loweredName, lowered)
                );
            }
            if (searchEmail) {
                return cb.like(loweredEmail, lowered);
            }
            return cb.like(loweredName, lowered);
        };
    }
}
