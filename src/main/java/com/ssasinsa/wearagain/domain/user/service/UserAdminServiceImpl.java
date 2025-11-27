package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListItemResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.time.ZoneOffset;
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
        // ??/?? ??? ??? ??? ?? ?? ??? ???? ???.
        // ?? ?? ?? ?? ?? ????? ?? ??.
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
        return new AdminParticipantDetailResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getTicketBalance(),
                user.getCreditBalance(),
                user.isSuspended(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().atOffset(ZoneOffset.UTC),
                user.getUpdatedAt() == null ? null : user.getUpdatedAt().atOffset(ZoneOffset.UTC)
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

}
