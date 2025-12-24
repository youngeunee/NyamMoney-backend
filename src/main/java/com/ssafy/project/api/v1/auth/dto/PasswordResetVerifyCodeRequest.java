package com.ssafy.project.api.v1.auth.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Data
@Setter
public class PasswordResetVerifyCodeRequest {
    private Long userId;
    private String loginId;
    private String verificationCode;
}
