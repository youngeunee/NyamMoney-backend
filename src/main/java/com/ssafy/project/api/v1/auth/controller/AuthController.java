package com.ssafy.project.api.v1.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final UserService uService;
	
	public AuthController(UserService uService) {
        this.uService = uService;
    }
	
	@PostMapping("login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest req){
		UserLoginResponse res = uService.login(req);
		
		return ResponseEntity.ok(res);
	}
}
