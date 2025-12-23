package com.ssafy.project.api.v1.challenge.chat.service;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;


@Service
public class ChallengeChatService {
    private final ChallengeParticipantMapper participantMapper;
    public ChallengeChatService(ChallengeParticipantMapper participantMapper) {
    	this.participantMapper = participantMapper;
    }

    public void validateParticipant(Long challengeId, Long userId) {
        int count = participantMapper.JOINEDParticipant(challengeId, userId);
        if (count == 0) {
            throw new RuntimeException("챌린지 참여자가 아닙니다.");
        }
    }
}
