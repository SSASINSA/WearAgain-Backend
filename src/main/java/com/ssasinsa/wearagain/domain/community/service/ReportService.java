package com.ssasinsa.wearagain.domain.community.service;

import com.ssasinsa.wearagain.domain.community.dto.request.ReportRequest;

public interface ReportService {

    void reportPost(ReportRequest request, Long userId);
}

