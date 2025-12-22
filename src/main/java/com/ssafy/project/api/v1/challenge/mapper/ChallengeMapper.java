package com.ssafy.project.api.v1.challenge.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeDetailResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeUpdateParam;
import com.ssafy.project.domain.challenge.model.ChallengeStatus;

@Mapper
public interface ChallengeMapper {

	List<ChallengeListItem> selectChallengeList(@Param("userId") Long userId);

	int insertChallenge(ChallengeCreateParam param);

	ChallengeStatus selectStatus(Long challengeId);

	LocalDate selectStartsAt(Long challengeId);

	void updateChallenge(ChallengeUpdateParam param);

	int softDeleteChallenge(Long challengeId);

	Long selectUserIdByChallengeId(Long challengeId);

	void updateChallengeStatusByDate(LocalDateTime now);

	List<Long> selectExpiredChallengeIds(LocalDateTime now);

	ChallengeDetailResponse selectChallengeDetail(@Param("challengeId") Long challengeId,
		    @Param("userId") Long userId);

}
