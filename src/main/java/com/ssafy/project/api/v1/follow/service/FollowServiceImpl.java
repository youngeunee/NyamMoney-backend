package com.ssafy.project.api.v1.follow.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.project.api.v1.follow.dto.FollowCreateResponse;
import com.ssafy.project.api.v1.follow.dto.FollowDto;
import com.ssafy.project.api.v1.follow.dto.FollowOperationResponse;
import com.ssafy.project.api.v1.follow.dto.FollowRequestApproveResponse;
import com.ssafy.project.api.v1.follow.dto.FollowRequestItem;
import com.ssafy.project.api.v1.follow.dto.FollowRequestsResponse;
import com.ssafy.project.api.v1.follow.dto.UserListResponse;
import com.ssafy.project.api.v1.follow.mapper.FollowMapper;
import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
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

	@Override
	@Transactional(readOnly = true)
	public FollowRequestsResponse getIncomingFollowRequests(Long userId) {
	    List<FollowRequestItem> items = followMapper.selectIncomingFollowRequests(userId);
	    return new FollowRequestsResponse("incoming", items.size(), items);
	}

	@Override
	@Transactional(readOnly = true)
	public FollowRequestsResponse getOutgoingFollowRequests(Long userId) {
	    List<FollowRequestItem> items = followMapper.selectOutgoingFollowRequests(userId);
	    return new FollowRequestsResponse("outgoing", items.size(), items);
	}

	@Override
	@Transactional
	public FollowRequestApproveResponse updateFollowRequest(Long userId, long requestId, Status status) {

	    // 400
	    if (status == null) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status 값이 없습니다.");
	    }

	    if (status != Status.ACCEPTED && status != Status.REJECTED) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status는 ACCEPTED 또는 REJECTED만 가능합니다.");
	    }

	    // 요청 조회
	    FollowDto follow = followMapper.getFollowRequest(requestId);

	    // 404: 요청 없음
	    if (follow == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "팔로우 요청이 존재하지 않습니다.");
	    }

	    // 404: 이미 처리됨
	    if (follow.getStatus() != Status.PENDING) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미 처리된 팔로우 요청입니다.");
	    }

	    // 403: 수신자만 처리 가능
	    if (!follow.getFolloweeId().equals(userId)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 팔로우 요청을 처리할 권한이 없습니다.");
	    }

	    // 상태 변경 (PENDING인 경우만)
	    int updated = followMapper.updateFollowRequestStatus(requestId, userId, status);

	    if (updated == 0) {
	        // 동시성(누가 먼저 처리) 등
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "요청이 존재하지 않거나 이미 처리되었습니다.");
	    }

	    // updatedAt 반영
	    FollowDto updatedFollow = followMapper.getFollowRequest(requestId);

	    return new FollowRequestApproveResponse(
	            updatedFollow.getFollowId(),
	            updatedFollow.getFollowerId(),
	            updatedFollow.getFolloweeId(),
	            updatedFollow.getStatus(),
	            updatedFollow.getUpdatedAt()
	    );
	}

	@Override
	public FollowOperationResponse deleteFollowRequest(Long userId, long requestId) {
		FollowDto follow = followMapper.getFollowRequest(requestId);
		
		if (follow == null) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "팔로우 요청이 존재하지 않습니다.");
	    }

	    // 403: 팔로우한 사람만 취소 가능
	    if (!follow.getFollowerId().equals(userId)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 팔로우 요청을 취소할 권한이 없습니다.");
	    }

	    // 400: 이미 처리된 요청은 취소 불가
	    if (follow.getStatus() != Status.PENDING) {
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 처리된 팔로우 요청은 취소할 수 없습니다.");
	    }

	    int deleted = followMapper.deletePendingFollowRequest(requestId, userId); // pending 중만 취소 가능
		
	    return new FollowOperationResponse(requestId, "CANCELED");
	}

	@Override
	@Transactional
	public FollowOperationResponse unfollow(Long userId, long targetUserId) {

	    // 403: 자기 자신
	    if (userId.equals(targetUserId)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "자기 자신을 언팔로우할 수 없습니다.");
	    }

	    int deleted = followMapper.deleteAcceptedFollow(userId, targetUserId);

	    // 404: 팔로우 중이 아님
	    if (deleted == 0) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "언팔로우할 대상이 존재하지 않습니다.");
	    }

	    return new FollowOperationResponse(targetUserId, "UNFOLLOWED");
	}

	@Override
	@Transactional
	public UserListResponse getFollowings(Long userId) {

		List<UserDetailResponse> followings = followMapper.selectFollowings(userId);
		
		return new UserListResponse(followings.size(), followings);
	}



}
