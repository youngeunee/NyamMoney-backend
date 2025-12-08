package com.ssafy.project.api.v1.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordUpdateRequest {

    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}