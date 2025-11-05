package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@Table(name = "event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_users_id", nullable = false)
    private AdminUser organizerAdmin;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Default
    private EventStatus status = EventStatus.DRAFT;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<EventOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<EventApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<EventImage> images = new ArrayList<>();

    @Column(name = "staff_code", length = 6)
    private String staffCode;

    @Column(name = "staff_code_issued_at")
    private LocalDateTime staffCodeIssuedAt;

    public static Event create(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            String location,
            EventStatus status,
            AdminUser organizerAdmin
    ) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .organizerAdmin(organizerAdmin)
                .startDate(startDate)
                .endDate(endDate)
                .location(location)
                .status(status == null ? EventStatus.DRAFT : status)
                .build();

        return event;
    }

    public void addOption(EventOption option) {
        options.add(option);
        option.assignEvent(this);
    }

    public void addApplication(EventApplication application) {
        applications.add(application);
    }

    public void addImage(EventImage image) {
        images.add(image);
        image.assignEvent(this);
    }

    public void changeStatus(EventStatus status) {
        this.status = status;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateLocation(String location) {
        this.location = location;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void assignImages(List<EventImage> images) {
        this.images.clear();
        if (images == null) {
            return;
        }
        images.forEach(this::addImage);
    }

    public void assignOptions(List<EventOption> options) {
        this.options.clear();
        if (options == null) {
            return;
        }
        options.forEach(this::addOption);
    }

    public void assignOrganizer(AdminUser organizerAdmin) {
        this.organizerAdmin = organizerAdmin;
    }

    public void updateStaffCode(String staffCode, LocalDateTime issuedAt) {
        this.staffCode = staffCode;
        this.staffCodeIssuedAt = issuedAt;
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
