package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryResponse {

    private LocalDateTime from;
    private LocalDateTime to;

    // 기간 전체 소비 합 (tx_type = EXPENSE)
    private Long totalExpense;

    // 기간 전체 시발비용 합 (tx_type = EXPENSE AND impulse_flag = 1)
    private Long totalImpulseExpense;
    
    private List<CategorySummaryItem> categorySummaries;

}