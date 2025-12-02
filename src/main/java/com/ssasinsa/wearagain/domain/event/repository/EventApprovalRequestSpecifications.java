package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.event.entity.EventApprovalRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventApprovalKeywordScope;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EventApprovalRequestSpecifications {

    private EventApprovalRequestSpecifications() {
    }

    public static Specification<EventApprovalRequest> pendingRequests() {
        return (root, query, cb) -> {
            Join<EventApprovalRequest, Event> eventJoin = root.join("event");
            Predicate unprocessed = cb.isNull(root.get("processedAt"));
            Predicate draftStatus = cb.equal(eventJoin.get("status"), EventStatus.DRAFT);
            return cb.and(unprocessed, draftStatus);
        };
    }

    public static Specification<EventApprovalRequest> keywordMatches(String keyword, EventApprovalKeywordScope scope) {
        if (!StringUtils.hasText(keyword)) {
            return (root, query, cb) -> cb.conjunction();
        }
        String normalized = keyword.toLowerCase();
        boolean searchTitle = scope == EventApprovalKeywordScope.ALL || scope == EventApprovalKeywordScope.TITLE;
        boolean searchDescription = scope == EventApprovalKeywordScope.ALL || scope == EventApprovalKeywordScope.DESCRIPTION;
        boolean searchRequester = scope == EventApprovalKeywordScope.ALL || scope == EventApprovalKeywordScope.REQUESTER;

        return (root, query, cb) -> {
            Join<EventApprovalRequest, Event> join = root.join("event");
            Join<EventApprovalRequest, AdminUser> adminJoin = searchRequester ? root.join("requestingAdmin") : null;
            List<Predicate> predicates = new ArrayList<>();
            if (searchTitle) {
                predicates.add(cb.like(cb.lower(join.get("title")), "%" + normalized + "%"));
            }
            if (searchDescription) {
                predicates.add(cb.like(cb.lower(join.get("description")), "%" + normalized + "%"));
            }
            if (searchRequester && adminJoin != null) {
                predicates.add(cb.like(cb.lower(adminJoin.get("name")), "%" + normalized + "%"));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
