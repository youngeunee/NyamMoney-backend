package com.ssafy.project.api.v1.challenge.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeJoinResponse;
import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeParticipantItem;
import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeParticipantListResponse;
import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeItem;
import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeListResponse;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;
import com.ssafy.project.domain.challenge.model.ChallengeStatus;
import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

@Service
public class ChallengeParticipantServiceImpl implements ChallengeParticipantService {
	private final ChallengeMapper cMapper;
	private final ChallengeParticipantMapper pMapper;
	public ChallengeParticipantServiceImpl(ChallengeMapper cMapper, ChallengeParticipantMapper pMapper) {
		this.cMapper = cMapper;
		this.pMapper = pMapper;
	}

	@Override
	public MyChallengeListResponse getMyChallenges(Long userId) {
		List<MyChallengeItem> items = pMapper.selectMyChallenges(userId);
        return new MyChallengeListResponse(items);
	}

	@Override
	public ChallengeJoinResponse joinChallenge(Long challengeId, Long userId) {
		// 챌린지 상태 검증
        ChallengeStatus status = cMapper.selectStatus(challengeId);
        if (status == null) {
            throw new IllegalArgumentException("존재하지 않는 챌린지입니다.");
        }

        if (status!=ChallengeStatus.UPCOMING && status!=ChallengeStatus.ACTIVE) {
            throw new IllegalStateException("진행 예정이거나 진행 중인 챌린지만 참여할 수 있습니다.");
        }

        // 이미 참여한 경우 체크, REFUNDED상태는 재참여 가능
        int exists = pMapper.existsParticipant(userId, challengeId);
        if (exists > 0) {
        	//pMapper.updateParticipantStatus(challengeId, userId, ChallengeParticipantStatus.JOINED);
        	// 참여자 상태 확인
        	ChallengeParticipantStatus pStatus = pMapper.selectParticipantStatus(challengeId, userId);
        	if (pStatus == ChallengeParticipantStatus.FAILED) {
                throw new IllegalStateException("진행 중 취소한 챌린지는 다시 참여할 수 없습니다.");
            }
        	if (pStatus != ChallengeParticipantStatus.REFUNDED) {
        		throw new IllegalStateException("이미 참여한 챌린지입니다.");
        	}
        	// REFUNDED만 재참여 허용
            if (pStatus == ChallengeParticipantStatus.REFUNDED) {
                pMapper.updateParticipantStatus(challengeId, userId, ChallengeParticipantStatus.JOINED);
            }
        } else {
        	// 참여 처리
        	pMapper.insertParticipant(challengeId, userId);
        }
        // 응답 생성
        return new ChallengeJoinResponse(challengeId, userId, "JOINED", 0.0);
	}
	
	@Override
	public void cancelChallengeJoin(Long challengeId, Long userId) {
		 // 참여한 사용자인지 확인
	    int participantCount = pMapper.existsParticipant(userId, challengeId);
	    if (participantCount == 0) {
	        throw new IllegalArgumentException("이 챌린지에 참여하지 않은 사용자입니다.");
	    }
	    
	    // 챌린지 상태 확인
        ChallengeStatus status = cMapper.selectStatus(challengeId);
        if (status == ChallengeStatus.ENDED) {
            throw new IllegalStateException("종료된 챌린지에서는 참여 취소가 불가능합니다.");
        }

        // 챌린지 시작일 확인
        LocalDate startsAt = cMapper.selectStartsAt(challengeId);

        // 시작 전 취소: REFUNDED로 상태 변경
        if (startsAt.isAfter(LocalDate.now())) {
            pMapper.updateParticipantStatus(challengeId, userId, ChallengeParticipantStatus.REFUNDED);
        }
        // 시작 후 취소: FAILED로 상태 변경
        else {
        	pMapper.updateParticipantStatus(challengeId, userId, ChallengeParticipantStatus.FAILED);
        }
	}

	@Override
	public ChallengeParticipantListResponse getChallengeParticipants(Long challengeId) {
		List<ChallengeParticipantItem> participants = pMapper.selectParticipantsByChallengeId(challengeId);
	    return new ChallengeParticipantListResponse(challengeId, participants);
	}
}
