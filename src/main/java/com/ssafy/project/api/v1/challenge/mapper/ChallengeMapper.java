package com.ssafy.project.api.v1.challenge.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListItem;
import com.ssafy.project.api.v1.challenge.dto.ChallengeUpdateParam;
import com.ssafy.project.domain.challenge.model.ChallengeStatus;

@Mapper
public interface ChallengeMapper {

	List<ChallengeListItem> selectChallengeList();

	int insertChallenge(ChallengeCreateParam param);

	ChallengeStatus selectStatus(Long challengeId);

	LocalDateTime selectStartsAt(Long challengeId);

	void updateChallenge(ChallengeUpdateParam param);

	int softDeleteChallenge(Long challengeId);

}
