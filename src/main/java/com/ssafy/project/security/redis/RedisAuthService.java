package com.ssafy.project.security.redis;

import org.springframework.stereotype.Service;

import com.ssafy.project.redis.repository.RefreshTokenRepository;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;


@Service
public class RedisAuthService {
	private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    public RedisAuthService(RefreshTokenRepository refreshTokenRepository, JWTUtil jwtUtil) {
    	this.refreshTokenRepository = refreshTokenRepository;
    	this.jwtUtil = jwtUtil;
    	
    }

    /**
     * userId 기준 로그인 상태 확인
     */
    public void validateLogin(Long userId) {
        boolean exists = refreshTokenRepository.existsByUserId(userId);

        if (!exists) {
            throw new RuntimeException("로그인 세션이 존재하지 않습니다.");
        }
    }

	public Long extractUserIdFromAccessToken(String accessToken) {
		if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("accessToken 없음");
        }

        Claims claims = jwtUtil.getClaims(accessToken);

        Long userId = claims.get("userId", Long.class);
        if (userId == null) {
            throw new RuntimeException("accessToken에 userId 없음");
        }

        return userId;
	}
}
