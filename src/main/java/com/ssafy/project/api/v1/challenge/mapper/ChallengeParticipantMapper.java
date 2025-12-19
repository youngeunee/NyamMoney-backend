package com.ssafy.project.api.v1.challenge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeItem;
import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

@Mapper
public interface ChallengeParticipantMapper {

	List<MyChallengeItem> selectMyChallenges(Long userId);

	int existsParticipant(Long challengeId, Long userId);

	void insertParticipant(Long challengeId, Long userId);

	void updateParticipantStatus(Long challengeId, ChallengeParticipantStatus status);

}
