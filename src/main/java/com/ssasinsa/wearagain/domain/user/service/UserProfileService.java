package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.user.dto.UserDisplayNameResponse;

public interface UserProfileService {

    UserDisplayNameResponse updateDisplayName(Long userId, String displayName);
}
