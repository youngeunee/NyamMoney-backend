package com.ssafy.project.api.v1.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Data
@Setter
public class PasswordResetConfirmRequest {

	@NotNull
    private Long userId;

    @NotBlank
    private String newPassword;
    
    @NotBlank
    private String newPasswordConfirm;
}
