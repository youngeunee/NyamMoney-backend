package com.ssafy.project.api.v1.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;
import com.ssafy.project.api.v1.user.dto.UserSignupResponse;
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
	
}
