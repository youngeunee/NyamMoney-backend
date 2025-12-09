package com.ssafy.project.api.v1.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.auth.service.AuthService;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final UserService uService;
	private final AuthService aService;
	private final JWTUtil jwtUtil;
	
	public AuthController(UserService uService, AuthService aService, JWTUtil jwtUtil) {
        this.uService = uService;
        this.aService = aService;
        this.jwtUtil = jwtUtil;
    }
	
	@PostMapping("login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest req){
		UserLoginResponse res = uService.login(req);
		
		return ResponseEntity.ok(res);
	}
	
	@Operation(
		    summary = "로그아웃",
		    description = "현재 로그인한 사용자의 refresh token을 삭제합니다.",
		    security = { @SecurityRequirement(name = "bearerAuth") }
		)
	@GetMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest req ){
		// access token 추출
		String header = req.getHeader("Authorization");
		if(header == null || !header.startsWith("Bearer ")) throw new IllegalAccessError("유효한 access token이 아닙니다.");
		
		
		String accessToken = header.substring(7);
		
		// token에서 userId 꺼내기
		Claims claims = jwtUtil.getClaims(accessToken);
		Long userId = claims.get("userId", Long.class);
		
		aService.logout(userId);
		
		return ResponseEntity.ok(Map.of("message", "로그아웃 하셨습니다."));
	}
}
