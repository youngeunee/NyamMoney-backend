package com.ssafy.project.domain.userMonthlyAgg.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMonthlyAggEntity {
    private Long monthlyId;
    private Long userId;
    private Integer year;
    private Integer month;
    private BigDecimal totalSpend;
    private BigDecimal impulseRatio;
    private String topCategory;
    private String summaryJson;
}
