package com.ssafy.project.api.v1.challenge.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.mapper.ChallengeMapper;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;
import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChallengeSchedulerService {
	private final ChallengeMapper cMapper;
    private final ChallengeParticipantMapper pMapper;
    public ChallengeSchedulerService(ChallengeMapper cMapper, ChallengeParticipantMapper pMapper) {
        this.cMapper = cMapper;
        this.pMapper = pMapper;
    }
    
    // 매일 자정에 챌린지 상태 처리
    @Scheduled(cron = "0 0 0 * * ?")  // 매일 자정 12:00
    public void checkExpiredChallenges() {
        // 현재 시간을 기준으로 종료된 챌린지 목록 조회
        LocalDateTime now = LocalDateTime.now();
        List<Long> expiredChallengeIds = cMapper.selectExpiredChallengeIds(now);

        // 종료된 챌린지에 대한 참여자 상태를 COMPLETED로 업데이트
        for (Long challengeId : expiredChallengeIds) {
            // 참여자 상태 변경
            pMapper.updateParticipantStatus(challengeId, ChallengeParticipantStatus.COMPLETED);
        }
        // 챌린지 상태 변경
        cMapper.updateChallengeStatusByDate(now);
        
        log.debug("스케줄러 실행");
    }
    
    public void testScheduler() {
    	checkExpiredChallenges();
    }
}
