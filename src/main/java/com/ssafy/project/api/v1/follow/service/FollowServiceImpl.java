package com.ssafy.project.api.v1.follow.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.project.api.v1.follow.dto.FollowCreateResponse;
import com.ssafy.project.api.v1.follow.dto.FollowDto;
import com.ssafy.project.api.v1.follow.mapper.FollowMapper;
import com.ssafy.project.domain.follow.model.Status;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FollowServiceImpl implements FollowService{
	
	private final FollowMapper followMapper;
	
	public FollowServiceImpl(FollowMapper followMapper) {
		this.followMapper = followMapper;
	}
	
	@Override
	@Transactional
	public FollowCreateResponse createFollow(Long userId, long targetUserId) {
		// 403: 자기 자신 팔로우 금
		if (Objects.equals(userId, targetUserId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자기 자신에게 팔로우 요청을 보낼 수 없습니다.");
		}
		
		// 404
		String visibility = followMapper.selectProfileVisibility(targetUserId);
		if(visibility == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}
		
		Status status;
		
		if(visibility.equals("PUBLIC")) status = Status.ACCEPTED;
		else if(visibility.equals("PROTECTED")) status = Status.PENDING;
		else status = Status.REJECTED;
		
		log.debug(visibility);
		
		followMapper.insertFollow(userId, targetUserId, status);
		
		FollowDto res = followMapper.selectByPair(userId, targetUserId);
        if (res == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "팔로우 요청 생성에 실패했습니다.");
        }

        return new FollowCreateResponse(
        		res.getFollowId(),
        		res.getFollowerId(),
        		res.getFolloweeId(),
        		res.getStatus(),
        		res.getCreatedAt()
        );
	}

}
