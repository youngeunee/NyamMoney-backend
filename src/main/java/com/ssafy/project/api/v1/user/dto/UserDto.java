package com.ssafy.project.api.v1.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.Role;
import com.ssafy.project.domain.user.model.ShareLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String loginId;
    private String email;
    private String pwHash;
    private String nickname;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
    private String timezone;
    private Long monthlyBudget;
    private Long triggerBudget;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Role role;
}