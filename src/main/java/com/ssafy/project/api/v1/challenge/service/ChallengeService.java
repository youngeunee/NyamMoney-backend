package com.ssafy.project.api.v1.challenge.service;

import org.apache.ibatis.javassist.NotFoundException;

import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeDetailResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeUpdateRequest;

import jakarta.validation.Valid;

public interface ChallengeService {

	ChallengeListResponse getChallengeList(Long userId);

	ChallengeCreateResponse createChallenge(@Valid ChallengeCreateRequest request, Long userId);

	void updateChallenge(Long challengeId, @Valid ChallengeUpdateRequest request, Long userId) throws Exception;

	void deleteChallenge(Long challengeId, Long userId) throws Exception;

	ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) throws NotFoundException;

}
