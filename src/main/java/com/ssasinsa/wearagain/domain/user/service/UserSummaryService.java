package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;

public interface UserSummaryService {

    UserSummaryResponse getUserSummary(Long userId);
}
