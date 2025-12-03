package com.ssafy.project.domain.challengeParticipant.entity;

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
public class ChallengeParticipantEntity {
    private Long participantId;
    private Long challengeId;
    private Long userId;
    private LocalDateTime joinedAt;
    private ChallengeParticipantStatus status;
}
