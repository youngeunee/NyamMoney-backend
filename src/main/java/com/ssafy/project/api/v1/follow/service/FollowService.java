package com.ssafy.project.api.v1.follow.service;

import com.ssafy.project.api.v1.follow.dto.FollowCreateResponse;
import com.ssafy.project.api.v1.follow.dto.FollowRequestsResponse;

public interface FollowService {

	FollowCreateResponse createFollow(Long userId, long targetUserId);

	FollowRequestsResponse getIncomingFollowRequests(Long userId);

	FollowRequestsResponse getOutgoingFollowRequests(Long userId);

}
