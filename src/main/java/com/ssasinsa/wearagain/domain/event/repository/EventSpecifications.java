package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventKeywordScope;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> statusIn(Collection<EventStatus> statuses) {
        return (root, query, builder) -> root.get("status").in(statuses);
    }

    public static Specification<Event> organizerIs(Long adminId) {
        return (root, query, builder) -> builder.equal(root.get("organizerAdmin").get("id"), adminId);
    }

    public static Specification<Event> keywordMatches(String keyword, EventKeywordScope scope) {
        if (!StringUtils.hasText(keyword) || scope == null) {
            return (root, query, builder) -> builder.conjunction();
        }
        String pattern = "%" + escapeLike(keyword.toLowerCase()) + "%";
        boolean searchTitle = scope == EventKeywordScope.ALL || scope == EventKeywordScope.TITLE;
        boolean searchDescription = scope == EventKeywordScope.ALL || scope == EventKeywordScope.DESCRIPTION;
        boolean searchLocation = scope == EventKeywordScope.ALL || scope == EventKeywordScope.LOCATION;

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchTitle) {
                predicates.add(builder.like(builder.lower(root.get("title")), pattern, '\\'));
            }
            if (searchDescription) {
                predicates.add(builder.like(builder.lower(root.get("description")), pattern, '\\'));
            }
            if (searchLocation) {
                predicates.add(builder.like(builder.lower(root.get("location")), pattern, '\\'));
            }
            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
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
