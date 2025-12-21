package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ChallengeUpdateParam {
	private Long challengeId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer periodDays;
}
