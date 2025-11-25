package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.entity.CommunityCategory;
import com.ssasinsa.wearagain.domain.community.repository.CommunityCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityCategoryInitializer {

    private final CommunityCategoryRepository communityCategoryRepository;

    private static final String[] DEFAULT_CATEGORIES = {"질문", "리뷰", "수선"};

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        initializeCategories();
    }

    public void initializeCategories() {
        for (String categoryName : DEFAULT_CATEGORIES) {
            if (communityCategoryRepository.findByName(categoryName).isEmpty()) {
                CommunityCategory category = CommunityCategory.create(categoryName);
                communityCategoryRepository.save(category);
                log.info("커뮤니티 카테고리가 생성되었습니다: {}", categoryName);
            }
        }
    }
}

