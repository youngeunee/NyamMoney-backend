package com.ssafy.project.api.v1.user.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.Role;
import com.ssafy.project.domain.user.model.ShareLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class UserDetailResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String email;
    private Long monthlyBudget;
    private Long triggerBudget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
    private Role role;
    private String name;
    private String phoneNumber;
}
