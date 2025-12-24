package com.ssafy.project.api.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupVerifyCodeRequest {
    private String email;
    private String verificationCode;
}
