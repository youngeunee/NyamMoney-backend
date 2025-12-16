package com.ssafy.project.api.v1.block.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.project.api.v1.follow.dto.FollowOperationResponse;
import com.ssafy.project.api.v1.follow.mapper.FollowMapper;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BlockServiceImpl implements BlockService {
	
	private final FollowMapper followMapper;
	private final UserMapper userMapper;
	
	public BlockServiceImpl(FollowMapper followMapper, UserMapper userMapper) {
		this.followMapper = followMapper;
		this.userMapper = userMapper;
	}
	
	@Override
	@Transactional
	public FollowOperationResponse block(Long userId, long targetUserId) {
		if(userId.equals(targetUserId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자기 자신을 차단할 수 없습니다.");
		
		UserDto targetUser = userMapper.findById(targetUserId);
		if(targetUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
		
		// block 처리
		followMapper.putBlocked(userId, targetUserId);
		
		// target -> 나 팔로우(요청) 하고 있었다면 끊기
		followMapper.deletePendingOrAccepted(targetUserId, userId);
		
		
        return new FollowOperationResponse(targetUserId, "BLOCKED");
	}

	@Override
	@Transactional
	public FollowOperationResponse unblock(Long userId, long targetUserId) {
		if(userId.equals(targetUserId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자기 자신을 차단 해제할 수 없습니다.");
		
		UserDto targetUser = userMapper.findById(targetUserId);
		if(targetUser == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
		
		int deleted = followMapper.deleteBlocked(userId, targetUserId);
		
        return new FollowOperationResponse(targetUserId, "UNBLOCKED");
	}

}
