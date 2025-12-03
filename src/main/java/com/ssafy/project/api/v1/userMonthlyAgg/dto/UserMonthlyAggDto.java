package com.ssafy.project.api.v1.userMonthlyAgg.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMonthlyAggDto {
    private Long monthlyId;
    private Long userId;
    private Integer year;
    private Integer month;
    private BigDecimal totalSpend;
    private BigDecimal impulseRatio;
    private String topCategory;
    private String summaryJson;
}
