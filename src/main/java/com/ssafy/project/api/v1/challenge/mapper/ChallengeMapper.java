package com.ssafy.project.api.v1.challenge.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateParam;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListItem;

@Mapper
public interface ChallengeMapper {

	List<ChallengeListItem> selectChallengeList();

	int insertChallenge(ChallengeCreateParam param);

}
