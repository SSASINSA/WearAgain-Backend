package com.ssasinsa.wearagain.domain.event.repository;

import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    @Query("""
            SELECT img FROM EventImage img
            WHERE img.event.id IN :eventIds
              AND img.displayOrder = (
                SELECT MIN(img2.displayOrder) FROM EventImage img2 WHERE img2.event = img.event
              )
            """)
    List<EventImage> findThumbnailsByEventIds(@Param("eventIds") Collection<Long> eventIds);
}
