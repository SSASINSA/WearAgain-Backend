package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "event_options")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_options_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "events_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_options_id")
    private EventOption parentOption;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 20)
    private String type;

    @Column
    private Integer quantity = 0;

    @Column
    private Integer remaining = 0;

    @OneToMany(mappedBy = "parentOption", fetch = FetchType.LAZY)
    private List<EventOption> childOptions = new ArrayList<>();

    @OneToMany(mappedBy = "eventOption", fetch = FetchType.LAZY)
    private List<EventApplication> applications = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private EventOption(Event event, EventOption parentOption, String name, String type, Integer quantity, Integer remaining) {
        this.event = event;
        this.parentOption = parentOption;
        this.name = name;
        this.type = type;
        this.quantity = quantity == null ? 0 : quantity;
        this.remaining = remaining == null ? 0 : remaining;
    }

    public static EventOption create(Event event, EventOption parentOption, String name, String type, Integer quantity, Integer remaining) {
        EventOption option = EventOption.builder()
                .event(event)
                .parentOption(parentOption)
                .name(name)
                .type(type)
                .quantity(quantity)
                .remaining(remaining)
                .build();
        if (event != null) {
            event.addOption(option);
        }
        if (parentOption != null) {
            parentOption.addChild(option);
        }
        return option;
    }

    private void addChild(EventOption option) {
        childOptions.add(option);
    }

    void addApplication(EventApplication application) {
        applications.add(application);
    }

    public void changeRemaining(int remaining) {
        this.remaining = remaining;
    }

    public void assignEvent(Event event) {
        this.event = event;
        if (!event.getOptions().contains(this)) {
            event.addOption(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventOption)) {
            return false;
        }
        EventOption other = (EventOption) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
