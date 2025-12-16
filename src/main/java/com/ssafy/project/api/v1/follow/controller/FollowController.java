package com.ssafy.project.api.v1.follow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.project.api.v1.follow.dto.FollowCreateResponse;
import com.ssafy.project.api.v1.follow.dto.FollowRequestApproveRequest;
import com.ssafy.project.api.v1.follow.dto.FollowRequestApproveResponse;
import com.ssafy.project.api.v1.follow.dto.FollowRequestsResponse;
import com.ssafy.project.api.v1.follow.service.FollowService;
import com.ssafy.project.security.auth.UserPrincipal;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {
	private final FollowService followService;
	
	public FollowController(FollowService followService) {
		this.followService = followService;
	}
	
	@PostMapping("/{targetUserId}")
	public ResponseEntity<FollowCreateResponse> createFollow(@PathVariable long targetUserId, @AuthenticationPrincipal UserPrincipal principal) {
		Long userId = principal.getUserId();
		
		FollowCreateResponse res = followService.createFollow(userId, targetUserId);
		return ResponseEntity.ok(res);
	}
	
	// /api/follow/follow-requests?direction=incoming/outgoing
	@GetMapping("/follow-requests")
	public ResponseEntity<FollowRequestsResponse> getFollowRequests(@RequestParam String direction, @AuthenticationPrincipal UserPrincipal principal){
		Long userId = principal.getUserId();
		
		if("incoming".equals(direction)) {
			return ResponseEntity.ok(followService.getIncomingFollowRequests(userId));
		}
		
		if("outgoing".equals(direction)) {
            return ResponseEntity.ok(followService.getOutgoingFollowRequests(userId));
		}
		
		throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "direction 값이 올바르지 않습니다. (incoming 또는 outgoing)"
        );
	}
	
	// /api/follow/follow-requests/{requestId}
	@PatchMapping("/follow-requests/{requestId}")
	public ResponseEntity<FollowRequestApproveResponse> updateFollowRequest(@PathVariable long requestId, @RequestBody FollowRequestApproveRequest req, @AuthenticationPrincipal UserPrincipal principal) {
		Long userId = principal.getUserId();
		
		FollowRequestApproveResponse res = followService.updateFollowRequest(userId, requestId, req.getStatus());
		
		return ResponseEntity.ok(res);
	}
	
}
