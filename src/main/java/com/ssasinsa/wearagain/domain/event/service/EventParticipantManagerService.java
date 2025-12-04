package com.ssasinsa.wearagain.domain.event.service;

import com.ssasinsa.wearagain.domain.auth.infrastructure.security.AdminAuthenticatedUser;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantListResponse;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminParticipantDetailResponse;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantKeywordScope;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantSort;
import com.ssasinsa.wearagain.domain.event.dto.manager.ManagerEventParticipantCancelRequest;
import com.ssasinsa.wearagain.domain.event.entity.EventApplicationStatus;
public interface EventParticipantManagerService {

    ManagerEventParticipantListResponse getParticipants(
            AdminAuthenticatedUser principal,
            Long eventId,
            EventApplicationStatus status,
            Boolean suspended,
            String keyword,
            ManagerEventParticipantKeywordScope keywordScope,
            int page,
            int size,
            ManagerEventParticipantSort sort
    );

    AdminParticipantDetailResponse getParticipantDetail(
            Long eventId,
            Long applicationId,
            AdminAuthenticatedUser principal
    );

    void cancelApplication(
            Long eventId,
            Long applicationId,
            ManagerEventParticipantCancelRequest request,
            AdminAuthenticatedUser principal
    );
}
