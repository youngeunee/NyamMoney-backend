package com.ssafy.project.api.v1.challenge.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.dto.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;

@Service
public class ChallengeServiceImpl implements ChallengeService {
	private ChallengeMapper challengeMapper;
	public ChallengeServiceImpl(ChallengeMapper challengeMapper) {
		this.challengeMapper = challengeMapper;
		
	}

	@Override
	public ChallengeListResponse getChallengeList() {
		List<ChallengeListItem> items = challengeMapper.selectChallengeList();
        return new ChallengeListResponse(items);
	}

}
