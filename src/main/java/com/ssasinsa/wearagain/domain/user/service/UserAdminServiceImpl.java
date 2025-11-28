package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.growth.GrowthConstants;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminImpactSummaryResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminMascotResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListItemResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.time.ZoneOffset;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final ImpactAnalyticsRepository impactAnalyticsRepository;
    private final UserGrowthRepository userGrowthRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantListResponse getParticipants(Boolean suspended, String sortBy, Pageable pageable) {
        Pageable pageableWithSort = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                resolveSort(sortBy)
        );

        Page<User> page = suspended == null
                ? userRepository.findAll(pageableWithSort)
                : userRepository.findAllBySuspended(suspended, pageableWithSort);

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
                page.hasPrevious()
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
        throw new UserException(UserErrorCode.FEATURE_NOT_AVAILABLE);
    }

    @Override
    public AdminParticipantDetailResponse updateSuspension(Long participantId, AdminParticipantSuspensionRequest request) {
        if (request.suspended() == null) {
            throw new UserException(UserErrorCode.INVALID_REQUEST);
        }
        User user = userRepository.findById(participantId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        user.updateSuspended(request.suspended());
        return toDetail(user);
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
                resolveMascot(userId)
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
                .map(growth -> new AdminMascotResponse(
                        growth.getCurrentLevel(),
                        growth.getExp(),
                        GrowthConstants.LEVEL_EXP_THRESHOLD,
                        growth.getMagicScissorCount(),
                        growth.getCycles()
                ))
                .orElse(null);
    }

    private BigDecimal scaleImpact(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

}
