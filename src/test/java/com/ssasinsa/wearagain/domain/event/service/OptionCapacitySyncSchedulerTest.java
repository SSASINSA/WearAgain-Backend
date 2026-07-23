package com.ssasinsa.wearagain.domain.event.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OptionCapacitySyncSchedulerTest {

    private static final EnumSet<EventApplicationStatus> ACTIVE_STATUSES =
            EnumSet.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN);

    @Mock
    private EventOptionRepository eventOptionRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    @Mock
    private OptionCapacityService optionCapacityService;

    private RedisResourceGuard redisResourceGuard;
    private OptionCapacitySyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        redisResourceGuard = new RedisResourceGuard();
        scheduler = new OptionCapacitySyncScheduler(
                eventOptionRepository,
                eventApplicationRepository,
                optionCapacityService,
                redisResourceGuard
        );
    }

    @Test
    void should_reset_with_fresh_database_usage_after_acquiring_lock() {
        EventOption option = option(1L, 10);
        when(eventOptionRepository.findIdsWithCapacityNotNull()).thenReturn(List.of(1L));
        when(eventOptionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(eventApplicationRepository.countByEventOptionIdAndStatusIn(1L, ACTIVE_STATUSES)).thenReturn(7L);
        when(optionCapacityService.reset(1L, 7L)).thenReturn(OptionCapacityService.ResetResult.RESET);

        scheduler.syncOptionCapacities();

        verify(eventApplicationRepository).countByEventOptionIdAndStatusIn(1L, ACTIVE_STATUSES);
        verify(optionCapacityService).reset(1L, 7L);
    }

    @Test
    void should_skip_option_when_request_holds_shared_lock() {
        EventOption option = option(1L, 10);
        when(eventOptionRepository.findIdsWithCapacityNotNull()).thenReturn(List.of(option.getId()));
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(option.getId());

        try (RedisResourceGuard.LockHandle ignored = redisResourceGuard.acquireRead(resourceKey)) {
            scheduler.syncOptionCapacities();
        }

        verify(eventOptionRepository, never()).findById(option.getId());
        verify(eventApplicationRepository, never()).countByEventOptionIdAndStatusIn(any(), any());
        verify(optionCapacityService, never()).reset(any(), anyLong());
    }

    @Test
    void should_skip_option_that_no_longer_has_capacity() {
        EventOption option = option(1L, null);
        when(eventOptionRepository.findIdsWithCapacityNotNull()).thenReturn(List.of(1L));
        when(eventOptionRepository.findById(1L)).thenReturn(Optional.of(option));

        scheduler.syncOptionCapacities();

        verify(eventApplicationRepository, never()).countByEventOptionIdAndStatusIn(any(), any());
        verify(optionCapacityService, never()).reset(any(), anyLong());
    }

    @Test
    void should_release_lock_when_reset_throws() throws Exception {
        EventOption option = option(1L, 10);
        RedisResourceKey resourceKey = RedisResourceKey.eventOption(1L);
        when(eventOptionRepository.findIdsWithCapacityNotNull()).thenReturn(List.of(1L));
        when(eventOptionRepository.findById(1L)).thenReturn(Optional.of(option));
        when(eventApplicationRepository.countByEventOptionIdAndStatusIn(1L, ACTIVE_STATUSES)).thenReturn(3L);
        when(optionCapacityService.reset(1L, 3L)).thenThrow(new RuntimeException());

        scheduler.syncOptionCapacities();

        Optional<RedisResourceGuard.LockHandle> acquired = redisResourceGuard.tryAcquireWrite(
                resourceKey,
                Duration.ZERO
        );
        try (RedisResourceGuard.LockHandle ignored = acquired.orElseThrow()) {
            verify(optionCapacityService).reset(1L, 3L);
        }
    }

    private EventOption option(Long id, Integer capacity) {
        EventOption option = EventOption.create(null, null, "option", 1, capacity);
        ReflectionTestUtils.setField(option, "id", id);
        return option;
    }
}
