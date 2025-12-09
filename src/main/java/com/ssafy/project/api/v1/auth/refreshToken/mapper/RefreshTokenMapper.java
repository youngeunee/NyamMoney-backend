package com.ssafy.project.api.v1.auth.refreshToken.mapper;

import com.ssafy.project.api.v1.auth.refreshToken.dto.RefreshTokenDto;

public interface RefreshTokenMapper {
	int insertRefreshToken(RefreshTokenDto token);
	
	int deleteByUserId(Long userId);
	
	RefreshTokenDto findByTokenHash(String tokenHash);
}
