package com.ssafy.project.api.v1.challenge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeUpdateParam;
import com.ssafy.project.api.v1.challenge.dto.ChallengeUpdateRequest;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;
import com.ssafy.project.domain.challenge.model.ChallengeStatus;


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

	@Override
	public void updateChallenge(Long challengeId, ChallengeUpdateRequest request) {
		// 상태 검증
        ChallengeStatus status = challengeMapper.selectStatus(challengeId);
        if (status != ChallengeStatus.UPCOMING) {
            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 수정할 수 없습니다.");
        }
        // 시간 검증
        LocalDateTime startsAt = challengeMapper.selectStartsAt(challengeId);
        if (!LocalDateTime.now().isBefore(startsAt)) {
            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 수정할 수 없습니다.");
        }
        // 기간 계산
        LocalDate start = request.getStartDate().toLocalDate();
	    LocalDate end = request.getEndDate().toLocalDate();

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("종료일은 시작일보다 빠를 수 없습니다.");
        }

        int periodDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

        // Update 실행
        ChallengeUpdateParam param = new ChallengeUpdateParam();
        param.setChallengeId(challengeId);
        param.setTitle(request.getTitle());
        param.setDescription(request.getDescription());
        param.setStartDate(request.getStartDate());
        param.setEndDate(request.getEndDate());
        param.setPeriodDays(periodDays);

        challengeMapper.updateChallenge(param);
	}

	@Override
	public void deleteChallenge(Long challengeId) {
		// 상태 검증
        ChallengeStatus status = challengeMapper.selectStatus(challengeId);
        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다.");
        }

        if (status != ChallengeStatus.UPCOMING) {
            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 삭제할 수 없습니다.");
        }

        // 시간 검증
        LocalDateTime startsAt = challengeMapper.selectStartsAt(challengeId);
        if (!LocalDateTime.now().isBefore(startsAt)) {
            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 삭제할 수 없습니다.");
        }

        // 3소프트 삭제
        int updated = challengeMapper.softDeleteChallenge(challengeId);
        if (updated == 0) {
            throw new IllegalArgumentException("챌린지 삭제에 실패했습니다.");
        }
	}

}
