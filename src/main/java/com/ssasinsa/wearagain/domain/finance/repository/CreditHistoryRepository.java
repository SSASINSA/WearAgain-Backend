package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
}
