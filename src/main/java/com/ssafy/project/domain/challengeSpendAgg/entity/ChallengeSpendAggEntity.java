package com.ssafy.project.domain.challengeSpendAgg.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeSpendAggEntity {
    private Long spendId;
    private Long challengeId;
    private Long userId;
    private BigDecimal totalSpend;
    private BigDecimal impulseSpend;
    private LocalDateTime calculatedAt;
}
