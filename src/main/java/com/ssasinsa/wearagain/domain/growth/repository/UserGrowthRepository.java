package com.ssasinsa.wearagain.domain.growth.repository;

import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface UserGrowthRepository extends JpaRepository<UserGrowth, Long> {

    Optional<UserGrowth> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ug FROM UserGrowth ug WHERE ug.user.id = :userId")
    Optional<UserGrowth> findByUserIdForUpdate(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}
