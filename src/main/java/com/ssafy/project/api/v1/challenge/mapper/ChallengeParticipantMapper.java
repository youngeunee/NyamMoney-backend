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

	// 챌린지 삭제하는 경우 그 챌린지 참여한 모두 상태 업데이트 (userId 안 받음)
	void updateParticipantStatusByDelete(Long challengeId, ChallengeParticipantStatus status);
	// 챌린지 참여 취소하는 경우 상태 업데이트 (userId 받음)
	void updateParticipantStatus(Long challengeId, Long userId, ChallengeParticipantStatus status);

}
