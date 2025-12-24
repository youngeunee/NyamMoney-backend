package com.ssafy.project.api.v1.user.dto;

import com.ssafy.project.domain.user.model.Role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String accessToken;
    private Role role;
    private String name;

    @JsonIgnore // refreshToken은 응답 바디에 포함하지 않음 (쿠키로만 전달)
    private String refreshToken;
}
