package com.ssasinsa.wearagain.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.user.dto.UserDisplayNameResponse;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileServiceImpl(userRepository);
    }

    @Test
    void should_update_display_name_when_user_exists() {
        // Given
        User user = User.create("user@wearagain.kr", "이전 이름", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        UserDisplayNameResponse response = userProfileService.updateDisplayName(1L, "새로운 이름");

        // Then
        assertThat(response.displayName()).isEqualTo("새로운 이름");
        assertThat(user.getDisplayName()).isEqualTo("새로운 이름");
    }

    @Test
    void should_throw_user_exception_when_user_not_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userProfileService.updateDisplayName(1L, "임시"))
                .isInstanceOf(UserException.class)
                .extracting(exception -> ((UserException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
