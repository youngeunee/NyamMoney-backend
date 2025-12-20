package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ChallengeCreateParam {
    private Long challengeId;     // useGeneratedKeys로 채워질 값 (입력 X)
    private String title;
    private String description;
    private Long budgetLimit;
    private LocalDate startDate;
    private LocalDate endDate;
    private int periodDays;
    private Long userId;
}
