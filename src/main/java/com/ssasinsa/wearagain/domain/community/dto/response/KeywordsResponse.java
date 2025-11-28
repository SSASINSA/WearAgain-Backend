package com.ssasinsa.wearagain.domain.community.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "게시글 키워드(카테고리) 목록 응답")
public record KeywordsResponse(
        @Schema(description = "키워드 목록", example = "[\"질문\", \"리뷰\", \"수선\"]")
        List<String> keywords
) {
}


