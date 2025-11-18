package com.ssasinsa.wearagain.domain.growth.repository;

import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGrowthRepository extends JpaRepository<UserGrowth, Long> {

    Optional<UserGrowth> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
