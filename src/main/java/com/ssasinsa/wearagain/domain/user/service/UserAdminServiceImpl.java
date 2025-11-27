package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantDetailResponse getParticipantDetail(Long participantId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Transactional(readOnly = true)
    public AdminParticipantStatsResponse getParticipantStats() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AdminParticipantDetailResponse updateParticipant(Long participantId, AdminParticipantUpdateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public AdminParticipantDetailResponse updateSuspension(Long participantId, AdminParticipantSuspensionRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
