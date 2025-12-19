package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ChallengeListItem {
    private Long challengeId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long userId; // 생성한 유저 id
}
