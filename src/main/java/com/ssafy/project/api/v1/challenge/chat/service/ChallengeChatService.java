package com.ssafy.project.api.v1.challenge.chat.service;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;
import com.ssafy.project.api.v1.challenge.chat.mapper.ChallengeChatMapper;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;


@Service
public class ChallengeChatService {
    private final ChallengeParticipantMapper participantMapper;
    private final ChallengeChatMapper challengeChatMapper;
    public ChallengeChatService(ChallengeParticipantMapper participantMapper, ChallengeChatMapper challengeChatMapper) {
    	this.participantMapper = participantMapper;
    	this.challengeChatMapper = challengeChatMapper;
    }

    public void validateParticipant(Long challengeId, Long userId) {
        int count = participantMapper.JOINEDParticipant(challengeId, userId);
        if (count == 0) {
            throw new RuntimeException("챌린지 참여자가 아닙니다.");
        }
    }
    
    public void saveMessage(ChallengeChatMessage message) {
    	challengeChatMapper.insertMessage(message);
    }

}
