package com.ssafy.project.api.v1.follow.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.follow.dto.FollowDto;
import com.ssafy.project.api.v1.follow.dto.FollowRequestItem;
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

	List<FollowRequestItem> selectIncomingFollowRequests(@Param("userId") Long userId);

	List<FollowRequestItem> selectOutgoingFollowRequests(@Param("userId") Long userId);

	int updateFollowRequestStatus(@Param("requestId") long requestId, @Param("userId") Long userId, @Param("status") Status status);

	FollowDto getFollowRequest(@Param("requestId")long requestId);
}