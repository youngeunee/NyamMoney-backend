package com.ssafy.project.api.v1.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDetailResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String email;
    private Long monthlyBudget;
    private Long triggerBudget;
    private LocalDateTime createdAt;
}
