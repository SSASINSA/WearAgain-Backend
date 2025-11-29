package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantStatsResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantSuspensionRequest;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {

    AdminParticipantListResponse getParticipants(Boolean suspended, String sortBy, Pageable pageable);

    AdminParticipantDetailResponse getParticipantDetail(Long participantId);

    AdminParticipantStatsResponse getParticipantStats();

    AdminParticipantDetailResponse updateParticipant(Long participantId, AdminParticipantUpdateRequest request);

    AdminParticipantDetailResponse updateSuspension(Long participantId, AdminParticipantSuspensionRequest request);
}
