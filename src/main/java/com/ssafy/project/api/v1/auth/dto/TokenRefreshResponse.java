package com.ssafy.project.api.v1.auth.dto;

import com.ssafy.project.domain.user.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenRefreshResponse {
    private Long userId;
    private String loginId;
    private String nickname;
    private Role role;
    private String name;
    private String accessToken;
    private String refreshToken;
}
