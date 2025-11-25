package com.ssasinsa.wearagain.domain.community.repository;

import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCategoryRepository extends JpaRepository<CommunityCategory, Long> {

    Optional<CommunityCategory> findByName(String name);
}

