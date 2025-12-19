package com.ssafy.project.api.v1.challenge.dto.participant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeJoinResponse {
    private Long challengeId;
    private Long userId;
    private String status;
    private Double progress; // 0.0 ~ 1.0
}
