package com.ssafy.project.api.v1.auth.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ssafy.project.api.v1.auth.dto.TokenRefreshResponse;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.mapper.UserMapper;
import com.ssafy.project.redis.repository.RefreshTokenRepository;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
//	private final RefreshTokenMapper rMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapper uMapper;
    private final JWTUtil jwtUtil;
    private final SignupEmailVerificationService signupEmailVerificationService;
    
	public AuthServiceImpl(RefreshTokenRepository refreshTokenRepository,
			UserMapper uMapper, JWTUtil jwtUtil,
			SignupEmailVerificationService signupEmailVerificationService) {
		this.refreshTokenRepository = refreshTokenRepository;
		this.uMapper = uMapper;
		this.jwtUtil = jwtUtil;
		this.signupEmailVerificationService = signupEmailVerificationService;
	}
	
	@Override
	@Transactional
	public void logout(Long userId) {
//		rMapper.deleteByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
	}

	// Refresh Token으로 access token 재발급 받기 
	@Override
	@Transactional
	public TokenRefreshResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 없습니다.");
        }

		Claims claims  = jwtUtil.getClaims(refreshToken);
		
		Long userId = claims.get("userId", Long.class);
		String loginId = claims.get("loginId", String.class);
        String jti = claims.get("jti", String.class);
        
        // 저장된 토큰인지 확인 
//        RefreshTokenDto rToken = rMapper.findByTokenHash(refreshToken);
		
        if (jti == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다.");
        }
        
        Map<Object, Object> stored = refreshTokenRepository.findByJti(jti);
        if (stored == null || stored.isEmpty()) {
            // 인덱스에 남아있을 수 있으니 정리
            refreshTokenRepository.deleteByJti(userId, jti);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다.");
        }

        String storedUserId = (String) stored.get("userId");
        String storedLoginId = (String) stored.get("loginId");

        if (storedUserId == null || !storedUserId.equals(String.valueOf(userId))) {
            refreshTokenRepository.deleteByJti(userId, jti);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다.");
        }
        if (storedLoginId != null && !storedLoginId.equals(loginId)) {
            refreshTokenRepository.deleteByJti(userId, jti);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 refresh token 입니다.");
        }

        UserDto user = uMapper.findById(userId);
        if (user == null) {
            refreshTokenRepository.deleteAllByUserId(userId);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "존재하지 않는 유저입니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(user);
        return new TokenRefreshResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                user.getRole(),
                user.getName(),
                newAccessToken,
                refreshToken);
        
//		log.debug("[AUTH] token userId = {}", userId);
//		log.debug("[AUTH] DB   userId = {}", rToken != null ? rToken.getUserId() : null);
//		log.debug("[AUTH] rToken == null ? {}", (rToken == null));
//        log.debug("[AUTH] DB에서 조회된 refreshToken: {}", rToken != null ? "'" + rToken.getTokenHash() + "'" : "null");
        
    
//        if (rToken == null || !rToken.getUserId().equals(userId)) {
//            throw new IllegalArgumentException("유효하지 않은 refresh token 입니다.");
//        }
//        
//        // 만료 여부 확인 
//        if (rToken.getExpiresAt().isBefore(LocalDateTime.now())) {
//            // 이미 만료된 토큰 → DB에서도 정리
//            rMapper.deleteByUserId(userId);
//            throw new IllegalArgumentException("refresh token 이 만료되었습니다. 다시 로그인 해주세요.");
//        }
//        
//        // 새로운 accessToken 발급
//        UserDto user = uMapper.findById(userId);
//        if(user == null) {
//        	rMapper.deleteByUserId(userId);
//        	throw new IllegalArgumentException("존재하지 않는 유저입니다.");
//        }
//        
//        String newAccessToken = jwtUtil.createAccessToken(user);
        
//        return new TokenRefreshResponse(newAccessToken, refreshToken);
	}

	@Override
	public boolean isVerified(String email) {
		return signupEmailVerificationService.isVerified(email);
	}
	
}
