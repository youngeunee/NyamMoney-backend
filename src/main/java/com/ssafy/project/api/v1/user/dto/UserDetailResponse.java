package com.ssafy.project.api.v1.user.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.Role;
import com.ssafy.project.domain.user.model.ShareLevel;

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
    private LocalDateTime upDatedAt;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
    private Role role;
    private String name;
    private String phoneNumber;
}
