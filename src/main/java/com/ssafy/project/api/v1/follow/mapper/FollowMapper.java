package com.ssafy.project.api.v1.follow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.follow.dto.FollowDto;
import com.ssafy.project.domain.follow.model.Status;

@Mapper
public interface FollowMapper {

    String selectProfileVisibility(@Param("targetUserId") long targetUserId);

    void insertFollow(
            @Param("userId") Long userId,
            @Param("targetUserId") long targetUserId,
            @Param("status") Status status
    );

    FollowDto selectByPair(
            @Param("userId") Long userId,
            @Param("targetUserId") long targetUserId
    );
}