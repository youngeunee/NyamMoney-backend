package com.ssafy.project.api.v1.user.dto;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.ShareLevel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserUpdateRequest {
    private String nickname;
    private String email;
    private Long monthlyBudget;
    private Long triggerBudget;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
}
