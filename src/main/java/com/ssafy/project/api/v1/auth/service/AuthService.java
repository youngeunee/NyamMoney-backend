package com.ssafy.project.api.v1.auth.service;

import com.ssafy.project.api.v1.auth.dto.TokenRefreshResponse;

public interface AuthService {
	public void logout(Long userId);
	
	public TokenRefreshResponse refresh(String refreshToken);

	public boolean isVerified(String email);
}
