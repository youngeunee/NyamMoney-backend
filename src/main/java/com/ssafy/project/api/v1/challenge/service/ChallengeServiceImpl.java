package com.ssafy.project.api.v1.challenge.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;

import jakarta.validation.Valid;

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

	@Override
	public ChallengeCreateResponse createChallenge(ChallengeCreateRequest request) {
		LocalDate start = request.getStartDate().toLocalDate();
	    LocalDate end = request.getEndDate().toLocalDate();
	    
	    if (end.isBefore(start)) {
	        throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
	    }
	    int periodDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
	    
		ChallengeCreateParam param = new ChallengeCreateParam();
	    param.setTitle(request.getTitle());
	    param.setDescription(request.getDescription());
	    param.setBudgetLimit(request.getBudgetLimit());
	    param.setStartDate(request.getStartDate());
	    param.setEndDate(request.getEndDate());
	    param.setPeriodDays(periodDays);

	    challengeMapper.insertChallenge(param);
	    
	    return new ChallengeCreateResponse(param.getChallengeId());
	    }

}
