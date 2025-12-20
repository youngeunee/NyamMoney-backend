package com.ssafy.project.api.v1.challenge.dto.participant;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeParticipantListResponse {
	private Long challengeId;
	private List<ChallengeParticipantItem> participants;

}
