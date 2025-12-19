package com.ssafy.project.api.v1.challenge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeDetailResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeUpdateParam;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeUpdateRequest;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;
import com.ssafy.project.domain.challenge.model.ChallengeStatus;
import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ChallengeServiceImpl implements ChallengeService {
	private final ChallengeMapper challengeMapper;
	private final ChallengeParticipantMapper pMapper;
	//private final ChallengeSchedulerService a;
	public ChallengeServiceImpl(ChallengeMapper challengeMapper, ChallengeParticipantMapper pMapper) {
		this.challengeMapper = challengeMapper;
		this.pMapper = pMapper;
		//this.a = a;
	}

	@Override
	public ChallengeListResponse getChallengeList() {
		List<ChallengeListItem> items = challengeMapper.selectChallengeList();
		// 스케줄러 작동하는지 확인
        //a.checkExpiredChallenges();
        return new ChallengeListResponse(items);
	}

	// 단일 챌린지 상세정보 조회
	@Override
	public ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) {
		// 챌린지 정보 조회
		ChallengeDetailResponse challengeDetail = challengeMapper.selectChallengeDetail(challengeId);
        if (challengeDetail == null) {
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다.");
        }

        // 참여자 수 조회
        int participantCount = pMapper.countParticipants(challengeId);

        // 사용자 참여 여부 확인 (로그인된 사용자가 참여한 챌린지인지)
        boolean isJoined = false;
        if (userId != null) {
            isJoined = pMapper.JOINEDParticipant(userId, challengeId) > 0;
            log.debug("count?: " + pMapper.JOINEDParticipant(userId, challengeId));
            log.debug("isJoined?" + isJoined);
        }

        // 응답 생성
        return new ChallengeDetailResponse(
            challengeDetail.getChallengeId(),
            challengeDetail.getTitle(),
            challengeDetail.getDescription(),
            challengeDetail.getStartDate(),
            challengeDetail.getEndDate(),
            participantCount,
            isJoined);
	}
	
	@Override
	public ChallengeCreateResponse createChallenge(ChallengeCreateRequest request, Long userId) {
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
	    param.setUserId(userId);

	    challengeMapper.insertChallenge(param);
	    
	    // 챌린지 생성한 유저 자동으로 참여하도록
	    pMapper.insertParticipant(param.getChallengeId(), userId);
	    
	    return new ChallengeCreateResponse(param.getChallengeId());
	    }

	@Override
	public void updateChallenge(Long challengeId, ChallengeUpdateRequest request, Long userId) throws Exception {
		// 생성한 유저만 수정 가능하도록
		// 챌린지Id 통해 userId 가져오기
		Long creatorId = challengeMapper.selectUserIdByChallengeId(challengeId);
        if (!creatorId.equals(userId)) {
            throw new Exception("생성자만 챌린지를 수정할 수 있습니다.");
        }
        
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
	public void deleteChallenge(Long challengeId, Long userId) throws Exception {
		// 생성한 유저만 삭제 가능하도록
		Long creatorId = challengeMapper.selectUserIdByChallengeId(challengeId);
        if (!creatorId.equals(userId)) {
            throw new Exception("생성자만 챌린지를 삭제할 수 있습니다.");
        }
		
		// 상태 검증
        ChallengeStatus status = challengeMapper.selectStatus(challengeId);
        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다.");
        }

        if (status != ChallengeStatus.UPCOMING) {
            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 삭제할 수 없습니다.");
        }

//        // 시간 검증
//        LocalDateTime startsAt = challengeMapper.selectStartsAt(challengeId);
//        if (!LocalDateTime.now().isBefore(startsAt)) {
//            throw new IllegalStateException("이미 시작되었거나 종료된 챌린지는 삭제할 수 없습니다.");
        
        
        // 삭제되기 전에 참여자들 REFUNDED로 바꾸기
        pMapper.updateParticipantStatusByDelete(challengeId, ChallengeParticipantStatus.REFUNDED);

        // 소프트 삭제
        int updated = challengeMapper.softDeleteChallenge(challengeId);
        if (updated == 0) {
            throw new IllegalArgumentException("챌린지 삭제에 실패했습니다.");
        }
	}

	

}
