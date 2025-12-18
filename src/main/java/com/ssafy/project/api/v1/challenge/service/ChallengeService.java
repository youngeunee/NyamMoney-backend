package com.ssafy.project.api.v1.challenge.service;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeUpdateRequest;

import jakarta.validation.Valid;

public interface ChallengeService {

	ChallengeListResponse getChallengeList();

	ChallengeCreateResponse createChallenge(@Valid ChallengeCreateRequest request);

	void updateChallenge(Long challengeId, @Valid ChallengeUpdateRequest request);

	void deleteChallenge(Long challengeId);

}
