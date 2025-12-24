package com.ssafy.project.api.v1.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.auth.dto.SignupSendCodeRequest;
import com.ssafy.project.api.v1.auth.dto.SignupVerifyCodeRequest;
import com.ssafy.project.api.v1.auth.service.SignupEmailVerificationService;
import com.ssafy.project.api.v1.user.service.UserService;

@RestController
@RequestMapping("/api/v1/auth/signup")
public class SignupEmailVerificationController {

    private final SignupEmailVerificationService signupEmailVerificationService;
    private final UserService userService;

    public SignupEmailVerificationController(
            SignupEmailVerificationService signupEmailVerificationService,
            UserService userService
    ) {
        this.signupEmailVerificationService = signupEmailVerificationService;
        this.userService = userService;
    }

    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(
            @RequestBody SignupSendCodeRequest req
    ) {
        userService.validateSignupEmailAvailable(req.getEmail());
        signupEmailVerificationService.sendVerificationCode(req.getEmail());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyCode(
            @RequestBody SignupVerifyCodeRequest req
    ) {
        signupEmailVerificationService.checkVerificationCode(
                req.getEmail(),
                req.getVerificationCode()
        );

        return ResponseEntity.ok().build();
    }
}