package com.ssafy.project.api.v1.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String accessToken;
}
