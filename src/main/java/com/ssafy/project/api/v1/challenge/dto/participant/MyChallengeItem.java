package com.ssafy.project.api.v1.challenge.dto.participant;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MyChallengeItem {
	private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Double progress;   // 0.0 ~ 1.0
}
