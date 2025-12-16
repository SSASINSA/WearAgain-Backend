package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.event.entity.EventApplication;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
import com.ssasinsa.wearagain.domain.event.entity.EventImage;
import com.ssasinsa.wearagain.domain.event.repository.EventApplicationRepository;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.GrowthConstants;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminImpactSummaryResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminMascotResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListItemResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListSummaryResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminRecentEventResponse;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import com.ssasinsa.wearagain.domain.user.repository.UserSpecifications;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final ImpactAnalyticsRepository impactAnalyticsRepository;
    private final UserGrowthRepository userGrowthRepository;
    private final EventApplicationRepository eventApplicationRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantListResponse getParticipants(
            Boolean suspended,
            String sortBy,
            String keyword,
            AdminParticipantKeywordScope keywordScope,
            Pageable pageable
    ) {
        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                resolveSort(sortBy)
        );

        Specification<User> specification = Specification.allOf(
                UserSpecifications.suspendedEquals(suspended),
                UserSpecifications.keywordMatches(keyword, keywordScope)
        );

        Page<User> page = userRepository.findAll(specification, pageableWithSort);

        List<AdminParticipantListItemResponse> items = page.getContent()
                .stream()
                .map(this::toListItem)
                .toList();

        return new AdminParticipantListResponse(
                items,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getSize(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious(),
                resolveListSummary()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantDetailResponse getParticipantDetail(Long participantId) {
        User user = userRepository.findById(participantId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return toDetail(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantStatsResponse getParticipantStats() {
        long totalParticipants = userRepository.count();
        long totalTickets = userRepository.sumTicketBalance();
        long totalCredits = userRepository.sumCreditBalance();
        return new AdminParticipantStatsResponse(totalParticipants, totalTickets, totalCredits);
    }

    @Override
    public AdminParticipantDetailResponse updateParticipant(Long participantId, AdminParticipantUpdateRequest request) {
        if (request == null || (request.ticketBalance() == null && request.creditBalance() == null)) {
            throw new UserException(UserErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findById(participantId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        int beforeTicketBalance = user.getTicketBalance();
        int beforeCreditBalance = user.getCreditBalance();

        if (request.ticketBalance() != null) {
            user.updateTicketBalance(request.ticketBalance());
        }
        if (request.creditBalance() != null) {
            user.updateCreditBalance(request.creditBalance());
        }

        log.info("[AdminUser] action=UPDATE_BALANCE participantId={} ticketBefore={} ticketAfter={} creditBefore={} creditAfter={}",
                participantId,
                beforeTicketBalance,
                user.getTicketBalance(),
                beforeCreditBalance,
                user.getCreditBalance());
        return toDetail(user);
    }

    @Override
    public AdminParticipantDetailResponse updateSuspension(Long participantId, AdminParticipantSuspensionRequest request) {
        if (request.suspended() == null) {
            throw new UserException(UserErrorCode.INVALID_REQUEST);
        }
        User user = userRepository.findById(participantId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        boolean beforeSuspended = user.isSuspended();
        user.updateSuspended(request.suspended());
        if (request.suspended()) {
            clearRefreshToken(user.getId());
        }
        log.info("[AdminUser] action=UPDATE_SUSPENSION participantId={} beforeSuspended={} afterSuspended={}",
                participantId,
                beforeSuspended,
                request.suspended());
        return toDetail(user);
    }

    private void clearRefreshToken(Long userId) {
        redisTemplate.delete(refreshTokenRedisKeyManager.userRefreshTokenKey(userId));
    }

    private Sort resolveSort(String sortBy) {
        String normalized = sortBy == null ? "CREATED_DESC" : sortBy.trim().toUpperCase();
        return switch (normalized) {
            case "CREATED_DESC" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case "CREATED_ASC" -> Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
            case "NAME_ASC" -> Sort.by(Sort.Order.asc("displayName"), Sort.Order.asc("id"));
            default -> throw new UserException(UserErrorCode.INVALID_REQUEST);
        };
    }

    private AdminParticipantDetailResponse toDetail(User user) {
        Long userId = user.getId();
        return new AdminParticipantDetailResponse(
                userId,
                user.getDisplayName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getTicketBalance(),
                user.getCreditBalance(),
                user.isSuspended(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().atOffset(ZoneOffset.UTC),
                user.getUpdatedAt() == null ? null : user.getUpdatedAt().atOffset(ZoneOffset.UTC),
                resolveImpactSummary(userId),
                resolveMascot(userId),
                resolveRecentEvents(userId)
        );
    }

    private AdminParticipantListItemResponse toListItem(User user) {
        return new AdminParticipantListItemResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getTicketBalance(),
                user.getCreditBalance(),
                user.isSuspended(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().atOffset(ZoneOffset.UTC)
        );
    }

    private AdminImpactSummaryResponse resolveImpactSummary(Long userId) {
        ImpactSummary summary = impactAnalyticsRepository.aggregateByUserId(userId);
        if (summary == null) {
            return AdminImpactSummaryResponse.zero();
        }
        return new AdminImpactSummaryResponse(
                scaleImpact(summary.co2Saved()),
                scaleImpact(summary.waterSaved()),
                scaleImpact(summary.energySaved())
        );
    }

    private AdminMascotResponse resolveMascot(Long userId) {
        return userGrowthRepository.findByUserId(userId)
                .map(growth -> {
                    int currentExp = growth.getExp();
                    int threshold = GrowthConstants.LEVEL_EXP_THRESHOLD;
                    int progressExp = threshold <= 0 ? currentExp : Math.floorMod(currentExp, threshold);
                    int remaining = threshold <= 0 ? 0 : Math.max(threshold - progressExp, 0);
                    BigDecimal percent = threshold <= 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(progressExp)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(threshold), 2, RoundingMode.HALF_UP);
                    return new AdminMascotResponse(
                            growth.getCurrentLevel(),
                            currentExp,
                            threshold,
                            remaining,
                            percent,
                            growth.getMagicScissorCount(),
                            growth.getCycles()
                    );
                })
                .orElse(null);
    }

    private AdminParticipantListSummaryResponse resolveListSummary() {
        LocalDateTime startOfCurrentMonth = LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfNextMonth = startOfCurrentMonth.plusMonths(1);

        long totalParticipants = userRepository.count();
        long totalTickets = userRepository.sumTicketBalance();
        long totalCredits = userRepository.sumCreditBalance();

        long participantsAtEndOfLastMonth = userRepository.countByCreatedAtBefore(startOfCurrentMonth);
        long participantsChange = totalParticipants - participantsAtEndOfLastMonth;

        long ticketsChangeThisMonth = defaultLong(ticketHistoryRepository.sumChangeAmountBetween(startOfCurrentMonth, startOfNextMonth));
        long ticketsAtEndOfLastMonth = totalTickets - ticketsChangeThisMonth;
        long ticketsChange = totalTickets - ticketsAtEndOfLastMonth;

        long creditsChangeThisMonth = defaultLong(creditHistoryRepository.sumChangeAmountBetween(startOfCurrentMonth, startOfNextMonth));
        long creditsAtEndOfLastMonth = totalCredits - creditsChangeThisMonth;
        long creditsChange = totalCredits - creditsAtEndOfLastMonth;

        return new AdminParticipantListSummaryResponse(
                totalParticipants,
                totalTickets,
                totalCredits,
                participantsChange,
                ticketsChange,
                creditsChange
        );
    }

    private BigDecimal scaleImpact(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private List<AdminRecentEventResponse> resolveRecentEvents(Long userId) {
        List<Long> applicationIds = eventApplicationRepository.findApplicationIdsForUser(
                userId,
                Set.of(EventApplicationStatus.APPLIED, EventApplicationStatus.CHECKED_IN),
                null,
                null,
                null,
                null,
                PageRequest.of(0, 5)
        );
        if (applicationIds.isEmpty()) {
            return List.of();
        }

        List<EventApplication> applications = eventApplicationRepository.findByIdsWithEventAndImages(applicationIds);
        Map<Long, EventApplication> byId = applications.stream()
                .collect(Collectors.toMap(
                        EventApplication::getId,
                        app -> app,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        return applicationIds.stream()
                .map(byId::get)
                .filter(app -> app != null && app.getEvent() != null)
                .map(app -> new AdminRecentEventResponse(
                        app.getEvent().getId(),
                        app.getEvent().getTitle(),
                        resolveEventThumbnailUrl(app.getEvent()),
                        app.getStatus(),
                        app.getEvent().getStartDate(),
                        app.getEvent().getEndDate(),
                        app.getCreatedAt() == null ? null : app.getCreatedAt().atOffset(ZoneOffset.UTC)
                ))
                .toList();
    }

    private String resolveEventThumbnailUrl(Event event) {
        if (event == null || event.getImages() == null || event.getImages().isEmpty()) {
            return null;
        }
        return event.getImages()
                .stream()
                .filter(image -> image != null && StringUtils.hasText(image.getUrl()))
                .sorted(Comparator.comparingInt(EventImage::getDisplayOrder))
                .map(EventImage::getUrl)
                .findFirst()
                .orElse(null);
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

}
