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
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSignupResponse {
	private Long userId;
    private String nickname;
    private LocalDateTime createdAt;
    private ProfileVisibility profileVisibility;
    private ShareLevel shareLevel;
    private Role role;
    private String name;
}
