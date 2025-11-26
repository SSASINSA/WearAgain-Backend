package com.ssasinsa.wearagain.domain.community.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.ReportRequest;
import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import com.ssasinsa.wearagain.domain.community.entity.Report;
import com.ssasinsa.wearagain.domain.community.entity.ReportStatus;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.ReportRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private CommunityPostRepository communityPostRepository;

    @Mock
    private UserRepository userRepository;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                reportRepository,
                communityPostRepository,
                userRepository
        );
    }

    @Test
    void should_create_report_when_valid_request() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        String reason = "부적절한 언어 사용";

        User reporter = User.create("reporter@wearagain.kr", "신고자", null);
        ReflectionTestUtils.setField(reporter, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(reporter, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE))
                .thenReturn(Optional.of(post));
        when(reportRepository.findByTargetTypeAndTargetIdAndReporterId("POST", postId, userId))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // When
        reportService.reportPost(request, userId);

        // Then
        ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository).save(reportCaptor.capture());
        Report savedReport = reportCaptor.getValue();

        assertThat(savedReport.getReporter().getId()).isEqualTo(userId);
        assertThat(savedReport.getTargetType()).isEqualTo("POST");
        assertThat(savedReport.getTargetId()).isEqualTo(postId);
        assertThat(savedReport.getReason()).isEqualTo(reason);
        assertThat(savedReport.getStatus()).isEqualTo(ReportStatus.PENDING);
        assertThat(post.getStatus()).isEqualTo(PostStatus.REPORTED);
    }

    @Test
    void should_throw_exception_when_post_not_found() {
        // Given
        Long userId = 1L;
        Long postId = 999L;
        String reason = "부적절한 언어 사용";

        User reporter = User.create("reporter@wearagain.kr", "신고자", null);
        ReflectionTestUtils.setField(reporter, "id", userId);

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportService.reportPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        // Given
        Long userId = 999L;
        Long postId = 100L;
        String reason = "부적절한 언어 사용";

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reportService.reportPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.INVALID_POST_DATA);

        verify(communityPostRepository, never()).findByIdAndStatusNot(any(), any());
        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void should_throw_exception_when_report_already_exists() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        String reason = "부적절한 언어 사용";

        User reporter = User.create("reporter@wearagain.kr", "신고자", null);
        ReflectionTestUtils.setField(reporter, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(reporter, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);

        Report existingReport = Report.create(reporter, "POST", postId, "이전 신고 사유");
        ReflectionTestUtils.setField(existingReport, "id", 1L);

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE))
                .thenReturn(Optional.of(post));
        when(reportRepository.findByTargetTypeAndTargetIdAndReporterId("POST", postId, userId))
                .thenReturn(Optional.of(existingReport));

        // When & Then
        assertThatThrownBy(() -> reportService.reportPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.REPORT_ALREADY_EXISTS);

        verify(reportRepository, never()).save(any(Report.class));
        assertThat(post.getStatus()).isNotEqualTo(PostStatus.REPORTED);
    }

    @Test
    void should_not_report_inactive_post() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        String reason = "부적절한 언어 사용";

        User reporter = User.create("reporter@wearagain.kr", "신고자", null);
        ReflectionTestUtils.setField(reporter, "id", userId);

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE))
                .thenReturn(Optional.empty()); // INACTIVE 게시글은 조회되지 않음

        // When & Then
        assertThatThrownBy(() -> reportService.reportPost(request, userId))
                .isInstanceOf(CommunityException.class)
                .extracting(throwable -> ((CommunityException) throwable).getErrorCode())
                .isEqualTo(CommunityErrorCode.POST_NOT_FOUND);

        verify(reportRepository, never()).save(any(Report.class));
    }

    @Test
    void should_change_post_status_to_reported() {
        // Given
        Long userId = 1L;
        Long postId = 100L;
        String reason = "부적절한 언어 사용";

        User reporter = User.create("reporter@wearagain.kr", "신고자", null);
        ReflectionTestUtils.setField(reporter, "id", userId);

        CommunityCategory category = CommunityCategory.create("review");
        ReflectionTestUtils.setField(category, "id", 1L);

        CommunityPost post = CommunityPost.create(reporter, category, "제목", "내용", null);
        ReflectionTestUtils.setField(post, "id", postId);
        // 초기 상태는 ACTIVE
        assertThat(post.getStatus()).isEqualTo(PostStatus.ACTIVE);

        ReportRequest request = new ReportRequest(postId, reason);

        when(userRepository.findById(userId)).thenReturn(Optional.of(reporter));
        when(communityPostRepository.findByIdAndStatusNot(postId, PostStatus.INACTIVE))
                .thenReturn(Optional.of(post));
        when(reportRepository.findByTargetTypeAndTargetIdAndReporterId("POST", postId, userId))
                .thenReturn(Optional.empty());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        // When
        reportService.reportPost(request, userId);

        // Then
        assertThat(post.getStatus()).isEqualTo(PostStatus.REPORTED);
    }
}

