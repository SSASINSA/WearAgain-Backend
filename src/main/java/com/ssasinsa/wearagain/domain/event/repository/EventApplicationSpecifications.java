package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;

public final class EventApplicationSpecifications {

    private EventApplicationSpecifications() {
    }

    public static Specification<EventApplication> organizerEquals(Long organizerId) {
        return (root, query, cb) -> {
            if (organizerId == null) {
                return cb.disjunction();
            }
            var eventJoin = root.join("event", JoinType.INNER);
            var adminJoin = eventJoin.join("organizerAdmin", JoinType.INNER);
            return cb.equal(adminJoin.get("id"), organizerId);
        };
    }

    public static Specification<EventApplication> eventIdIn(Collection<Long> eventIds) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(eventIds)) {
                return cb.conjunction();
            }
            var eventJoin = root.join("event", JoinType.INNER);
            return eventJoin.get("id").in(eventIds);
        };
    }

    public static Specification<EventApplication> eventIdEquals(Long eventId) {
        return (root, query, cb) -> {
            if (eventId == null) {
                return cb.conjunction();
            }
            var eventJoin = root.join("event", JoinType.INNER);
            return cb.equal(eventJoin.get("id"), eventId);
        };
    }

    public static Specification<EventApplication> eventCodesIn(Collection<String> eventCodes) {
        return (root, query, cb) -> {
            if (CollectionUtils.isEmpty(eventCodes)) {
                return cb.conjunction();
            }
            var eventJoin = root.join("event", JoinType.INNER);
            return eventJoin.get("staffCode").in(eventCodes);
        };
    }

    public static Specification<EventApplication> statusEquals(EventApplicationStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<EventApplication> keywordMatches(String keyword, ManagerEventParticipantKeywordScope scope) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(keyword) || scope == null) {
                return cb.conjunction();
            }
            String lowered = "%" + keyword.toLowerCase() + "%";
            boolean searchEmail = scope == ManagerEventParticipantKeywordScope.ALL || scope == ManagerEventParticipantKeywordScope.EMAIL;
            boolean searchName = scope == ManagerEventParticipantKeywordScope.ALL || scope == ManagerEventParticipantKeywordScope.NAME;
            if (!searchEmail && !searchName) {
                return cb.conjunction();
            }
            var userJoin = root.join("user", JoinType.LEFT);
            if (searchEmail && searchName) {
                return cb.or(
                        cb.like(cb.lower(userJoin.get("email")), lowered),
                        cb.like(cb.lower(userJoin.get("displayName")), lowered)
                );
            }
            if (searchEmail) {
                return cb.like(cb.lower(userJoin.get("email")), lowered);
            }
            return cb.like(cb.lower(userJoin.get("displayName")), lowered);
        };
    }

    public static Specification<EventApplication> userSuspended(Boolean suspended) {
        return (root, query, cb) -> {
            if (suspended == null) {
                return cb.conjunction();
            }
            var userJoin = root.join("user", JoinType.LEFT);
            return cb.equal(userJoin.get("suspended"), suspended);
        };
    }
}
