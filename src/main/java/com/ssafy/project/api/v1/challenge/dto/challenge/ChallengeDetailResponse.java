package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeDetailResponse {
	private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int participantCount;
    private boolean isJoined;
}
