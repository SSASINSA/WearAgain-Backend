package com.ssasinsa.wearagain.domain.growth.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GrowthInitializerTest {

    @Mock
    private UserGrowthRepository userGrowthRepository;

    private GrowthInitializer growthInitializer;

    @BeforeEach
    void setUp() {
        growthInitializer = new GrowthInitializer(userGrowthRepository);
    }

    @Test
    void should_notCreate_whenGrowthAlreadyExists() {
        User user = createUser(1L);
        when(userGrowthRepository.findByUserId(1L)).thenReturn(Optional.of(UserGrowth.create(user)));

        growthInitializer.initialize(user);

        verify(userGrowthRepository, never()).save(any(UserGrowth.class));
    }

    @Test
    void should_createGrowth_whenMissing() {
        User user = createUser(1L);
        when(userGrowthRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userGrowthRepository.save(any(UserGrowth.class))).thenAnswer(invocation -> invocation.getArgument(0));

        growthInitializer.initialize(user);

        verify(userGrowthRepository).save(any(UserGrowth.class));
    }

    @Test
    void should_ignoreDuplicateCreation_whenConcurrentInsertOccurs() {
        User user = createUser(1L);
        when(userGrowthRepository.findByUserId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(UserGrowth.create(user)));
        when(userGrowthRepository.save(any(UserGrowth.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatCode(() -> growthInitializer.initialize(user)).doesNotThrowAnyException();

        verify(userGrowthRepository, times(2)).findByUserId(1L);
    }

    private User createUser(Long id) {
        User user = User.create("user@example.com", "user", null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
