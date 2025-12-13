package com.ssafy.project.api.v1.user.dto;

import com.ssafy.project.domain.user.model.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String accessToken;
    private String refreshToken;
    private Role role;
    private String name;
}
