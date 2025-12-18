package com.ssafy.project.api.v1.challenge.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChallengeListItem {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
