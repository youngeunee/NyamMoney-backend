package com.ssafy.project.api.v1.user.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;
import com.ssafy.project.api.v1.user.dto.UserSignupResponse;
import com.ssafy.project.api.v1.user.dto.UserUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateResponse;
import com.ssafy.project.api.v1.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService uService;
	
	public UserController(UserService uService){
		this.uService = uService;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest req){
		UserDto user = uService.signup(req);
		
		UserSignupResponse res = new UserSignupResponse(user.getUserId(), user.getNickname(), user.getCreatedAt());
		
		return ResponseEntity.ok(res);
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long userId) {
		UserDetailResponse res = uService.getUserDetail(userId);
		return ResponseEntity.ok(res);
	}
	
	@PatchMapping("/{userId}")
	public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest req) {
        UserUpdateResponse res = uService.updateUser(userId, req);
        return ResponseEntity.ok(res);
    }
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable Long userId){
		uService.deleteUser(userId);
		return ResponseEntity.ok(Map.of("message", "탈퇴되었습니다."));
	}
}
