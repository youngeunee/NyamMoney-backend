package com.ssafy.project.api.v1.user.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.ShareLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserUpdateResponse {
    private Long userId;
    private String loginId;
    private String nickname;
    private String email;
    private Long monthlyBudget;
    private Long triggerBudget;
    private LocalDateTime updatedAt;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
}
