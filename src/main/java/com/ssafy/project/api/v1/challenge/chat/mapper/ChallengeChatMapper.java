package com.ssafy.project.api.v1.challenge.chat.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;

@Mapper
public interface ChallengeChatMapper {

	void insertMessage(ChallengeChatMessage message);

}
