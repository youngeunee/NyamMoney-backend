package com.ssafy.project.api.v1.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.auth.refreshToken.mapper.RefreshTokenMapper;

@Service
public class AuthServiceImpl implements AuthService {
	private final RefreshTokenMapper rMapper;
	
	public AuthServiceImpl(RefreshTokenMapper rMapper) {
		this.rMapper = rMapper;
	}
	
	@Override
	@Transactional
	public void logout(Long userId) {
		rMapper.deleteByUserId(userId);
	}
	
}
