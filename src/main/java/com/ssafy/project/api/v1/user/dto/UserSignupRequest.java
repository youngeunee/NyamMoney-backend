package com.ssafy.project.api.v1.user.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.domain.user.model.ShareLevel;

import jakarta.validation.constraints.Email;

@Getter
@Setter
public class UserSignupRequest {

    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String passwordConfirm;

    @NotBlank
    private String nickname;

    @Email
    private String email;
    
    private ProfileVisibility profileVisibility;
    
    private ShareLevel shareLevel;
    
    private Long monthlyBudget;
    private Long triggerBudget;
}
