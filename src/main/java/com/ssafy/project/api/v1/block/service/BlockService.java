package com.ssafy.project.api.v1.block.service;

import com.ssafy.project.api.v1.follow.dto.FollowOperationResponse;
import com.ssafy.project.api.v1.follow.dto.UserListResponse;

public interface BlockService {
	
	/**
     * targetUserId 사용자를 차단.
     * - (me -> target) 관계를 BLOCKED로 upsert
     * - (target -> me) 방향의 PENDING/ACCEPTED 관계는 정리(끊기)
     */
	FollowOperationResponse block(Long userId, long targetUserId);

	FollowOperationResponse unblock(Long userId, long targetUserId);

	UserListResponse getBlocks(Long userId);

}
