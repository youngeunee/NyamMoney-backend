package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDate;

import com.ssafy.project.domain.challenge.model.ChallengeStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeDetailResponse {
	private Long challengeId;
	private Long userId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private int participantCount;
    private boolean isJoined;
    
    private ChallengeStatus status;
}
