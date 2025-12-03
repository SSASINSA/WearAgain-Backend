package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.user.dto.UserDisplayNameResponse;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;

    @Override
    public UserDisplayNameResponse updateDisplayName(Long userId, String displayName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        String sanitizedDisplayName = displayName.trim();
        user.updateDisplayName(sanitizedDisplayName);
        return new UserDisplayNameResponse(user.getDisplayName());
    }
}
