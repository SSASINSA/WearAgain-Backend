package com.ssasinsa.wearagain.domain.event.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Getter
@Entity
@Table(name = "event_option")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class EventOption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_option_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_event_option_id")
    private EventOption parentOption;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column
    private Integer capacity;

    @OneToMany(mappedBy = "parentOption", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    @BatchSize(size = 50)
    @Fetch(FetchMode.SUBSELECT)
    private List<EventOption> childOptions = new ArrayList<>();

    @OneToMany(mappedBy = "eventOption", fetch = FetchType.LAZY)
    @Default
    @BatchSize(size = 50)
    private List<EventApplication> applications = new ArrayList<>();

    public static EventOption create(
            Event event,
            EventOption parentOption,
            String name,
            String type,
            int displayOrder,
            Integer capacity
    ) {
        EventOption option = EventOption.builder()
                .event(event)
                .parentOption(parentOption)
                .name(name)
                .type(type)
                .displayOrder(displayOrder)
                .capacity(capacity)
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

    public boolean isSelectable() {
        return childOptions == null || childOptions.isEmpty();
    }

    public void assignEvent(Event event) {
        this.event = event;
    }

    public void assignChildren(List<EventOption> children) {
        this.childOptions.clear();
        if (children == null) {
            return;
        }
        children.forEach(child -> {
            child.parentOption = this;
            this.childOptions.add(child);
        });
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
