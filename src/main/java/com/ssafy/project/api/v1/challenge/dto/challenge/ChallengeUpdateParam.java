package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChallengeUpdateParam {
	private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer periodDays;
}
