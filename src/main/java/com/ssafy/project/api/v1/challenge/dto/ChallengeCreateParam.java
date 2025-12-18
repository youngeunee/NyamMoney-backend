package com.ssafy.project.api.v1.challenge.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChallengeCreateParam {
    private Long challengeId;     // useGeneratedKeys로 채워질 값 (입력 X)
    private String title;
    private String description;
    private Long budgetLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int periodDays;
}
