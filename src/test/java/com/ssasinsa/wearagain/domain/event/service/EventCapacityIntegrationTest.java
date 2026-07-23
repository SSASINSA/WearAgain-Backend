package com.ssasinsa.wearagain.domain.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventApplyRequest;
import com.ssasinsa.wearagain.domain.event.dto.request.EventCancelRequest;
import com.ssasinsa.wearagain.domain.event.dto.response.EventApplyResponse;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.exception.EventErrorCode;
import com.ssasinsa.wearagain.domain.event.exception.EventException;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import com.ssasinsa.wearagain.domain.event.service.OptionCapacityService.ResetResult;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceGuard;
import com.ssasinsa.wearagain.global.common.redis.RedisResourceKey;
import com.ssasinsa.wearagain.support.RedisTestContainerSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class EventCapacityIntegrationTest extends RedisTestContainerSupport {

    private static final int OPTION_CAPACITY = 1;
    private static final String CAPACITY_KEY_PREFIX = "event:option:used:";

    @Autowired
    private EventUserService eventUserService;
    @Autowired
    private EventParticipantManagerService eventParticipantManagerService;
    @Autowired
    private OptionCapacityService optionCapacityService;
    @Autowired
    private EventApplicationRepository eventApplicationRepository;
    @Autowired
    private EventOptionRepository eventOptionRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AdminUserRepository adminUserRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RedisResourceGuard redisResourceGuard;

    private Long adminId;
    private Long userId;
    private Long secondUserId;
    private Long eventId;
    private Long optionId;

    @BeforeEach
    void setUp() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            AdminUser admin = adminUserRepository.save(
                    AdminUser.createApproved(
                            "event-capacity-admin@test.com",
                            "encoded",
                            "매니저",
                            AdminRole.MANAGER
                    )
            );
            User user = userRepository.save(
                    User.create("event-capacity-user@test.com", "사용자", null)
            );
            User secondUser = userRepository.save(
                    User.create("event-capacity-second-user@test.com", "두 번째 사용자", null)
            );
            Event event = Event.create(
                    "행사 정원 통합 테스트",
                    "행사 신청과 취소의 Redis 정합성 검증",
                    LocalDate.now(),
                    LocalDate.now().plusDays(1),
                    "서울",
                    EventStatus.OPEN,
                    admin,
                    null,
                    null,
                    1
            );
            EventOption option = EventOption.create(event, null, "테스트 옵션", 1, OPTION_CAPACITY);
            eventRepository.saveAndFlush(event);

            adminId = admin.getId();
            userId = user.getId();
            secondUserId = secondUser.getId();
            eventId = event.getId();
            optionId = option.getId();
        });

        assertThat(optionCapacityService.reset(optionId, 0)).isEqualTo(ResetResult.RESET);
    }

    @AfterEach
    void tearDown() {
        if (optionId == null) {
            deleteDatabaseFixtures();
            return;
        }

        try (RedisResourceGuard.LockHandle ignored = redisResourceGuard.acquireRead(resourceKey())) {
            try {
                deleteDatabaseFixtures();
            } finally {
                deleteKeys(redisTemplate, capacityKey());
            }
        }
    }

    private void deleteDatabaseFixtures() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            if (eventId != null) {
                eventApplicationRepository.deleteAllInBatch(
                        eventApplicationRepository.findAllWithUserByEventId(eventId)
                );
            }
            if (optionId != null) {
                eventOptionRepository.deleteAllByIdInBatch(List.of(optionId));
            }
            if (eventId != null) {
                eventRepository.deleteAllByIdInBatch(List.of(eventId));
            }
            if (userId != null) {
                userRepository.deleteAllByIdInBatch(List.of(userId));
            }
            if (secondUserId != null) {
                userRepository.deleteAllByIdInBatch(List.of(secondUserId));
            }
            if (adminId != null) {
                adminUserRepository.deleteAllByIdInBatch(List.of(adminId));
            }
        });
    }

    @Test
    void should_keep_capacity_consistent_after_apply_and_cancel_commit() {
        EventApplyResponse response = apply();

        assertThat(findApplication(response.applicationId()).getStatus())
                .isEqualTo(EventApplicationStatus.APPLIED);
        assertRedisUsed(1);

        eventUserService.cancel(response.applicationId(), new EventCancelRequest("일정 변경"), userId);

        assertThat(findApplication(response.applicationId()).getStatus())
                .isEqualTo(EventApplicationStatus.CANCELED);
        assertRedisUsed(0);
    }

    @Test
    void should_restore_capacity_when_apply_transaction_rolls_back() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            apply();
            status.setRollbackOnly();
        });

        assertThat(eventApplicationRepository.findAllWithUserByEventId(eventId)).isEmpty();
        assertRedisUsed(0);
    }

    @Test
    void should_reject_second_application_when_capacity_is_full() {
        apply();

        assertThatThrownBy(() -> apply(secondUserId))
                .isInstanceOf(EventException.class)
                .extracting(throwable -> ((EventException) throwable).getErrorCode())
                .isEqualTo(EventErrorCode.EVENT_CAPACITY_EXCEEDED);

        assertThat(eventApplicationRepository.findAllWithUserByEventId(eventId)).hasSize(1);
        assertRedisUsed(1);
    }

    @Test
    void should_not_release_capacity_when_cancel_transaction_rolls_back() {
        EventApplyResponse response = apply();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            eventUserService.cancel(response.applicationId(), new EventCancelRequest("취소 롤백"), userId);
            status.setRollbackOnly();
        });

        assertThat(findApplication(response.applicationId()).getStatus())
                .isEqualTo(EventApplicationStatus.APPLIED);
        assertRedisUsed(1);
    }

    @Test
    void should_release_capacity_after_manager_rejection_commit() {
        EventApplyResponse response = apply();
        AdminAuthenticatedUser principal = new AdminAuthenticatedUser(
                adminId,
                "event-capacity-admin@test.com",
                "매니저",
                AdminRole.MANAGER
        );

        eventParticipantManagerService.cancelApplication(
                eventId,
                response.applicationId(),
                new ManagerEventParticipantCancelRequest("관리자 거절"),
                principal
        );

        assertThat(findApplication(response.applicationId()).getStatus())
                .isEqualTo(EventApplicationStatus.REJECTED);
        assertRedisUsed(0);
    }

    @Test
    void should_not_release_capacity_when_manager_rejection_rolls_back() {
        EventApplyResponse response = apply();
        markApplicationCheckedIn(response.applicationId());
        AdminAuthenticatedUser principal = new AdminAuthenticatedUser(
                adminId,
                "event-capacity-admin@test.com",
                "매니저",
                AdminRole.MANAGER
        );

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            eventParticipantManagerService.cancelApplication(
                    eventId,
                    response.applicationId(),
                    new ManagerEventParticipantCancelRequest("관리자 거절 롤백"),
                    principal
            );
            status.setRollbackOnly();
        });

        assertThat(findApplication(response.applicationId()).getStatus())
                .isEqualTo(EventApplicationStatus.CHECKED_IN);
        assertRedisUsed(1);
    }

    @Test
    void should_initialize_missing_capacity_key_when_application_commits() {
        try (RedisResourceGuard.LockHandle ignored = redisResourceGuard.acquireRead(resourceKey())) {
            redisTemplate.delete(capacityKey());
            assertThat(redisTemplate.hasKey(capacityKey())).isFalse();

            EventApplyResponse response = apply();

            assertThat(findApplication(response.applicationId()).getStatus())
                    .isEqualTo(EventApplicationStatus.APPLIED);
            assertRedisUsed(1);
        }
    }

    private EventApplyResponse apply() {
        return apply(userId);
    }

    private EventApplyResponse apply(Long targetUserId) {
        return eventUserService.apply(
                eventId,
                new EventApplyRequest(optionId, "통합 테스트 신청"),
                targetUserId
        );
    }

    private EventApplication findApplication(Long applicationId) {
        return eventApplicationRepository.findById(applicationId).orElseThrow();
    }

    private void markApplicationCheckedIn(Long applicationId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status ->
                findApplication(applicationId).checkIn(LocalDateTime.now())
        );
    }

    private void assertRedisUsed(long expected) {
        assertThat(redisTemplate.opsForValue().get(capacityKey()))
                .isEqualTo(String.valueOf(expected));
    }

    private String capacityKey() {
        return CAPACITY_KEY_PREFIX + optionId;
    }

    private RedisResourceKey resourceKey() {
        return RedisResourceKey.eventOption(optionId);
    }
}
