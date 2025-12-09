package com.ssafy.project.api.v1.auth.refreshToken.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.auth.refreshToken.dto.RefreshTokenDto;

@Mapper
public interface RefreshTokenMapper {
	int insertRefreshToken(RefreshTokenDto token);
	
	int deleteByUserId(Long userId);
	
	RefreshTokenDto findByTokenHash(String tokenHash);
}
