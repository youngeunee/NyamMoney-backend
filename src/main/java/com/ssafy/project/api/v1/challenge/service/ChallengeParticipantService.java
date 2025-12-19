package com.ssafy.project.api.v1.challenge.service;

import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeJoinResponse;
import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeListResponse;

public interface ChallengeParticipantService {

	MyChallengeListResponse getMyChallenges(Long userId);

	ChallengeJoinResponse joinChallenge(Long challengeId, Long userId);
	void cancelChallengeJoin(Long userId, Long challengeId);

}
