package com.ssasinsa.wearagain.domain.finance.repository;

import com.ssasinsa.wearagain.domain.finance.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
}
