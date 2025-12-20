package com.ssafy.project.api.v1.challenge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeParticipantItem;
import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeItem;
import com.ssafy.project.domain.challengeParticipant.ChallengeParticipantStatus;

@Mapper
public interface ChallengeParticipantMapper {

	List<MyChallengeItem> selectMyChallenges(Long userId);

	int existsParticipant(@Param("userId") Long userId, @Param("challengeId") Long challengeId);

	void insertParticipant(@Param("challengeId") Long challengeId, @Param("userId") Long userId);

	// 챌린지 삭제하는 경우 그 챌린지 참여한 모두 상태 업데이트 (userId 안 받음)
	void updateParticipantStatusByDelete(Long challengeId, ChallengeParticipantStatus status);
	
	// 챌린지 참여 취소하는 경우 상태 업데이트 (userId 받음)
	void updateParticipantStatus(@Param("challengeId") Long challengeId, @Param("userId") Long userId,
			@Param("status") ChallengeParticipantStatus status);

	ChallengeParticipantStatus selectParticipantStatus(@Param("challengeId") Long challengeId, @Param("userId") Long userId);

	int countParticipants(Long challengeId);

	int JOINEDParticipant(Long userId, Long challengeId);
	// 특정 챌린지 참여중인 사용자 목록 조회
	List<ChallengeParticipantItem> selectParticipantsByChallengeId(Long challengeId);

}
