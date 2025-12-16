package com.ssafy.project.api.v1.block.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.block.service.BlockService;
import com.ssafy.project.api.v1.follow.dto.FollowOperationResponse;
import com.ssafy.project.security.auth.UserPrincipal;

@RestController
@RequestMapping("api/v1/blocks")
public class BlockController {
	
	private final BlockService blockService;
	
	public BlockController(BlockService blockService) {
		this.blockService = blockService;
	}
	
	@PutMapping("/{targetUserId}")
	public ResponseEntity<FollowOperationResponse> block(@PathVariable long targetUserId, @AuthenticationPrincipal UserPrincipal principal){
		Long userId = principal.getUserId();
		
		FollowOperationResponse res = blockService.block(userId, targetUserId);
		
		return ResponseEntity.ok(res);
	}
	
	@DeleteMapping("/{targetUserId}")
	public ResponseEntity<FollowOperationResponse> unblock(@PathVariable long targetUserId, @AuthenticationPrincipal UserPrincipal principal){
		Long userId = principal.getUserId();
		
		FollowOperationResponse res = blockService.unblock(userId, targetUserId);
		
		return ResponseEntity.ok(res);
	}
}
