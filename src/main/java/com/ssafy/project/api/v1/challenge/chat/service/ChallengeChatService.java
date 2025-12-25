package com.ssafy.project.api.v1.challenge.chat.service;

import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;
import com.ssafy.project.api.v1.challenge.chat.mapper.ChallengeChatMapper;
import com.ssafy.project.api.v1.challenge.mapper.ChallengeParticipantMapper;
import com.ssafy.project.api.v1.user.mapper.UserMapper;
import com.ssafy.project.domain.user.model.Role;
import com.ssafy.project.api.v1.user.dto.UserDto;


@Service
public class ChallengeChatService {
    private final ChallengeParticipantMapper participantMapper;
    private final ChallengeChatMapper challengeChatMapper;
    private final UserMapper userMapper;
    public ChallengeChatService(ChallengeParticipantMapper participantMapper, ChallengeChatMapper challengeChatMapper,
    		UserMapper userMapper) {
    	this.participantMapper = participantMapper;
    	this.challengeChatMapper = challengeChatMapper;
    	this.userMapper = userMapper;
    }

    public void validateParticipant(Long challengeId, Long userId) {
    	if (isAdmin(userId)) {
    		return;
    	}
        int count = participantMapper.JOINEDParticipant(challengeId, userId);
        if (count == 0) {
            throw new RuntimeException("챌린지 참여자가 아닙니다.");
        }
    }
    
    public void saveMessage(ChallengeChatMessage message) {
    	challengeChatMapper.insertMessage(message);
    }

	public List<ChallengeChatMessage> getChatHistory(Long challengeId, Long userId, String before, int size) {
	    validateParticipant(challengeId, userId);

	    LocalDateTime beforeTime = parseBefore(before);
	    if (beforeTime == null) {
	    	beforeTime = LocalDateTime.now();
	    }

	    List<ChallengeChatMessage> descList =
	        challengeChatMapper.findByChallengeIdBefore(challengeId, beforeTime, size);

	    // 최신순으로 가져오므로 클라이언트 표시용으로 오름차순 정렬
	    Collections.sort(descList, Comparator.comparing(ChallengeChatMessage::getSentAt));
	    return descList;
	}

	private LocalDateTime parseBefore(String before) {
		if (before == null || before.isBlank()) return null;
		try {
			return LocalDateTime.parse(before);
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private boolean isAdmin(Long userId) {
		if (userId == null) return false;
		UserDto user = userMapper.findById(userId);
		return user != null && user.getRole() == Role.ADMIN;
	}

}
