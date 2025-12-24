package com.ssafy.project.api.v1.auth.service;

import java.security.NoSuchAlgorithmException;

import com.ssafy.project.api.v1.auth.dto.PasswordResetConfirmRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetSendCodeRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetSendCodeResponse;
import com.ssafy.project.api.v1.auth.dto.PasswordResetVerifyCodeRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetVerifyCodeResponse;

public interface PasswordResetService {
	
    PasswordResetSendCodeResponse sendVerificationCode(PasswordResetSendCodeRequest req) throws NoSuchAlgorithmException;
    
    PasswordResetVerifyCodeResponse checkVerificationCode(PasswordResetVerifyCodeRequest req) throws NoSuchAlgorithmException;
    
    void changePassword(PasswordResetConfirmRequest req);
}