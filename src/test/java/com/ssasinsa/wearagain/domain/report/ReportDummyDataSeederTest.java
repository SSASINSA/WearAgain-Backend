package com.ssasinsa.wearagain.domain.report;

import com.ssasinsa.wearagain.domain.auth.entity.AdminRole;
import com.ssasinsa.wearagain.domain.auth.entity.AdminUser;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.AdminUserRepository;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventOption;
import com.ssasinsa.wearagain.domain.event.entity.EventStatus;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventOptionRepository;
import com.ssasinsa.wearagain.domain.event.repository.EventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReportDummyDataSeederTest {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventOptionRepository eventOptionRepository;

    @Autowired
    private EventApplicationRepository eventApplicationRepository;

    @Test
    void seedClosedEventWithCheckedInApplications() {
//        AdminUser superAdmin = adminUserRepository.findByEmail("admin@wearagain.kr")
//                .orElseThrow(() -> new IllegalStateException("슈퍼 어드민이 없습니다. 먼저 생성하세요."));
//
//        Event event = eventRepository.save(Event.create(
//                "리포트 테스트 이벤트",
//                "종료된 이벤트 리포트 테스트용",
//                LocalDate.now().minusDays(2),
//                LocalDate.now().minusDays(1),
//                "서울 테스트홀",
//                EventStatus.CLOSED,
//                superAdmin,
//                null,
//                null
//        ));
//
//        EventOption option = eventOptionRepository.save(EventOption.create(event, null, "기본 옵션", 1, 200));
//
//        LocalTime startTime = LocalTime.of(9, 0);
//        int perDay = 20;
//        long runId = System.currentTimeMillis();
//
//        // dayOffset: 1 = 어제, 2 = 그제
//        for (int dayOffset = 1; dayOffset <= 2; dayOffset++) {
//            LocalDate baseDate = LocalDate.now().minusDays(dayOffset);
//            for (int i = 0; i < perDay; i++) {
//                String email = "dummy" + runId + "_" + dayOffset + "_" + i + "@wearagain.kr";
//                User user = userRepository.save(User.create(email, "참가자" + dayOffset + "_" + i, null));
//                EventApplication application = EventApplication.create(
//                        user,
//                        event,
//                        option,
//                        EventApplicationStatus.CHECKED_IN,
//                        null
//                );
//                LocalDateTime checkedInAt = LocalDateTime.of(baseDate, startTime.plusHours(i % 10));
//                application.checkIn(checkedInAt);
//                eventApplicationRepository.save(application);
//            }
//        }

        // 실행 시 데이터가 실제 DB에 저장됩니다. 필요 시 직접 정리하세요.
    }
}
