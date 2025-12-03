package com.ssafy.project.api.v1.challengeParticipant.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeParticipantDto {
    private Long participantId;
    private Long challengeId;
    private Long userId;
    private LocalDateTime joinedAt;
    private ChallengeParticipantStatus status;
}
