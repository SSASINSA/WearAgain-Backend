package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "events_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "staff_code", length = 64)
    private String staffCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventImage> images = new ArrayList<>();

    public static Event create(
            String title,
            String description,
            String staffCode,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            EventStatus status,
            List<String> imageUrls
    ) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .staffCode(staffCode)
                .startDate(startDate)
                .endDate(endDate)
                .location(location)
                .status(status)
                .build();

        if (imageUrls != null) {
            int order = 0;
            for (String imageUrl : imageUrls) {
                EventImage.create(event, imageUrl, order++);
            }
        }

        return event;
    }

    void addOption(EventOption option) {
        options.add(option);
    }

    void addApplication(EventApplication application) {
        applications.add(application);
    }

    void addImage(EventImage image) {
        images.add(image);
    }

    public void changeStatus(EventStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Event)) {
            return false;
        }
        Event other = (Event) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
