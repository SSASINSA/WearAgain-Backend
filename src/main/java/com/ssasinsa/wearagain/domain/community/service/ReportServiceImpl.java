package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.community.dto.request.ReportRequest;
import com.ssasinsa.wearagain.domain.community.entity.CommunityPost;
import com.ssasinsa.wearagain.domain.community.entity.PostStatus;
import com.ssasinsa.wearagain.domain.community.entity.Report;
import com.ssasinsa.wearagain.domain.community.exception.CommunityErrorCode;
import com.ssasinsa.wearagain.domain.community.exception.CommunityException;
import com.ssasinsa.wearagain.domain.community.repository.CommunityPostRepository;
import com.ssasinsa.wearagain.domain.community.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String TARGET_TYPE_POST = "POST";

    private final ReportRepository reportRepository;
    private final CommunityPostRepository communityPostRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void reportPost(ReportRequest request, Long userId) {
        User reporter = userRepository.findById(userId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.INVALID_POST_DATA));

        CommunityPost post = communityPostRepository.findByIdAndStatusNot(request.postId(), PostStatus.INACTIVE)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.POST_NOT_FOUND));

        // 중복 신고 체크
        reportRepository.findByTargetTypeAndTargetIdAndReporterId(TARGET_TYPE_POST, request.postId(), userId)
                .ifPresent(report -> {
                    throw new CommunityException(CommunityErrorCode.REPORT_ALREADY_EXISTS);
                });

        // 신고 생성
        Report report = Report.create(reporter, TARGET_TYPE_POST, request.postId(), request.reason());
        reportRepository.save(report);

        // 게시글 상태를 REPORTED로 변경
        post.report();

        log.info("게시글 신고 완료: postId={}, userId={}, reportId={}", request.postId(), userId, report.getId());
    }
}

