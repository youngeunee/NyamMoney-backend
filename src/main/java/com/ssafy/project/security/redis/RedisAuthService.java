package com.ssafy.project.security.redis;

import org.springframework.stereotype.Service;

import com.ssafy.project.redis.repository.RefreshTokenRepository;


@Service
public class RedisAuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    public RedisAuthService(RefreshTokenRepository refreshTokenRepository) {
    	this.refreshTokenRepository = refreshTokenRepository;
    	
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
}
