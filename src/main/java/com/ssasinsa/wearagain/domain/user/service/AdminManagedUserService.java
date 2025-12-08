package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.AdminStatus;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserKeywordScope;
import com.ssasinsa.wearagain.domain.user.dto.admin.AdminManagedUserListResponse;

public interface AdminManagedUserService {

    AdminManagedUserListResponse getAdminUsers(
            AdminStatus status,
            String keyword,
            AdminManagedUserKeywordScope keywordScope,
            int page,
            int size,
            String sortBy
    );

    void softDeleteAdminUser(Long adminUserId, Long actorAdminId);
}
