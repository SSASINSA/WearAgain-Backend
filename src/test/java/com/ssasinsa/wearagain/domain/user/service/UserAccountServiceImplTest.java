package com.ssasinsa.wearagain.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserOAuthAccountRepository;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.auth.infrastructure.RefreshTokenRedisKeyManager;
import com.ssasinsa.wearagain.domain.user.exception.UserErrorCode;
import com.ssasinsa.wearagain.domain.user.exception.UserException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserOAuthAccountRepository userOAuthAccountRepository;

    @Mock
    private RefreshTokenRedisKeyManager refreshTokenRedisKeyManager;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountServiceImpl(
                userRepository,
                userOAuthAccountRepository,
                refreshTokenRedisKeyManager,
                redisTemplate
        );
    }

    @Test
    void should_withdraw_user_when_active() {
        // Given
        User user = User.create("user@wearagain.kr", "테스터", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRedisKeyManager.userRefreshTokenKey(1L)).thenReturn("auth:user:1");

        // When
        userAccountService.withdraw(1L);

        // Then
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getEmail()).startsWith("withdrawn_1_").endsWith("@wearagain.local");
        assertThat(user.getDisplayName()).isEqualTo("탈퇴한 사용자");
        assertThat(user.getProfileImageUrl()).isNull();
        assertThat(user.getTicketBalance()).isZero();
        assertThat(user.getCreditBalance()).isZero();
        verify(userOAuthAccountRepository).deleteAllByUser(user);
        verify(redisTemplate).delete("auth:user:1");
    }

    @Test
    void should_throw_exception_when_user_not_found() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAccountService.withdraw(1L))
                .isInstanceOf(UserException.class)
                .extracting(exception -> ((UserException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void should_throw_exception_when_already_withdrawn() {
        // Given
        User user = User.create("user@wearagain.kr", "이름", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "deleted", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userAccountService.withdraw(1L))
                .isInstanceOf(UserException.class)
                .extracting(exception -> ((UserException) exception).getErrorCode())
                .isEqualTo(UserErrorCode.USER_ALREADY_WITHDRAWN);
    }
}
