package com.ssafy.project.api.v1.challenge.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ssafy.project.domain.challenge.model.ChallengeStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeDto {
    private Long challengeId;
    private String title;
    private BigDecimal budgetLimit;
    private Integer periodDays;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private ChallengeStatus status;
    private BigDecimal entryFee;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
