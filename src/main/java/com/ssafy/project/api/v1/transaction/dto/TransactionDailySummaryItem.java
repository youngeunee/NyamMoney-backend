package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDailySummaryItem {
    private LocalDate date;
    private Long totalExpense;
    private Long totalImpulseExpense;
    private List<CategorySummaryItem> categorySummaries;
}