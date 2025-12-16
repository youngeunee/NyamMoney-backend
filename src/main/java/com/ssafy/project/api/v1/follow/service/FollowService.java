package com.ssafy.project.api.v1.follow.service;

import com.ssafy.project.api.v1.follow.dto.FollowCreateResponse;

public interface FollowService {

	FollowCreateResponse createFollow(Long userId, long targetUserId);

}
