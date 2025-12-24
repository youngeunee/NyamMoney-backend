package com.ssafy.project.api.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@Data
@AllArgsConstructor
@Setter
@NoArgsConstructor
public class PasswordResetSendCodeResponse {
    private long resendAvailableAt;
    private int expiresInSeconds;
}