package com.ssasinsa.wearagain.domain.user.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.repository.TicketHistoryRepository;
import com.ssasinsa.wearagain.domain.user.dto.UserSummaryResponse;
import com.ssasinsa.wearagain.global.exception.CommonErrorCode;
import com.ssasinsa.wearagain.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserSummaryServiceImpl implements UserSummaryService {

    private final UserRepository userRepository;
    private final TicketHistoryRepository ticketHistoryRepository;

    @Override
    public UserSummaryResponse getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.UNAUTHORIZED));
        int totalTicketChange = convertToInt(ticketHistoryRepository.sumChangeAmountByUserId(userId));
        return new UserSummaryResponse(
                user.getDisplayName(),
                user.getTicketBalance(),
                user.getCreditBalance(),
                totalTicketChange
        );
    }

    private int convertToInt(Long value) {
        if (value == null) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return value.intValue();
    }
}
