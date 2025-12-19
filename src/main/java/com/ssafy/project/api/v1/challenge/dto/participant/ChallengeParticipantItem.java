package com.ssafy.project.api.v1.challenge.dto.participant;

import java.time.LocalDateTime;

import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeParticipantItem {
	private Long userId;
    private String nickname;
    private LocalDateTime joinedAt;
    private ChallengeParticipantStatus status;
    private double progress;
}
