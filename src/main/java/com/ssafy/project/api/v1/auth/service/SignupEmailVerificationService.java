package com.ssafy.project.api.v1.auth.service;

public interface SignupEmailVerificationService {

    void sendVerificationCode(String email);

    void checkVerificationCode(String email, String code);

    boolean isVerified(String email);

    void clear(String email);
}