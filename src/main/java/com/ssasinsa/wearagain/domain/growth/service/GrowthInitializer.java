package com.ssasinsa.wearagain.domain.growth.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrowthInitializer {

    private final UserGrowthRepository userGrowthRepository;

    public void initialize(User user) {
        Objects.requireNonNull(user, "user must not be null");
        Long userId = Objects.requireNonNull(user.getId(), "user id must not be null");

        if (userGrowthRepository.existsByUserId(userId)) {
            return;
        }

        try {
            userGrowthRepository.save(UserGrowth.create(user));
        } catch (DataIntegrityViolationException exception) {
            if (!userGrowthRepository.existsByUserId(userId)) {
                throw exception;
            }
        }
    }
}
