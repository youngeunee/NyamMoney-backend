package com.ssafy.project.api.v1.auth.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.auth.caller.AuthMailCaller;
import com.ssafy.project.api.v1.auth.dto.VerificationState;
import com.ssafy.project.api.v1.user.service.UserService;

@Service
public class SignupEmailVerificationServiceImpl
        implements SignupEmailVerificationService {

    private final AuthMailCaller mailCaller;
    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "auth:signup:";
    private static final long CODE_TTL_MINUTES = 10;
    private static final long VERIFIED_TTL_MINUTES = 5;
    private static final long RESEND_COOLDOWN_MS = 60_000;
    private static final int MAX_ATTEMPT = 5;

    public SignupEmailVerificationServiceImpl(
            AuthMailCaller mailCaller,
            StringRedisTemplate redisTemplate
    ) {
        this.mailCaller = mailCaller;
        this.redisTemplate = redisTemplate;
    }

    private String codeKey(String email) {
        return PREFIX + "code:" + email;
    }

    private String metaKey(String email) {
        return PREFIX + "meta:" + email;
    }

    private String verifiedKey(String email) {
        return PREFIX + "verified:" + email;
    }
    @Override
    public void sendVerificationCode(String email) {
        long now = System.currentTimeMillis();

        VerificationState state =
                VerificationState.from(
                        redisTemplate.opsForValue().get(metaKey(email))
                );

        if (state.getResendBlockedUntil() > now) {
            throw new IllegalStateException("인증번호 재전송 대기 중입니다.");
        }

        String code = mailCaller.makeCode();
        mailCaller.sendSignupCode(email, code);

        redisTemplate.opsForValue().set(
                codeKey(email),
                code,
                CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        VerificationState next =
                new VerificationState(0, now + RESEND_COOLDOWN_MS);

        redisTemplate.opsForValue().set(
                metaKey(email),
                next.toValue(),
                CODE_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    @Override
    public void checkVerificationCode(String email, String code) {
        // 인증번호 조회
        String savedCode = redisTemplate.opsForValue().get(codeKey(email));
        if (savedCode == null) {
            throw new IllegalStateException("인증번호가 만료되었습니다.");
        }

        VerificationState state =
                VerificationState.from(
                        redisTemplate.opsForValue().get(metaKey(email))
                );

        if (state.getAttemptCount() >= MAX_ATTEMPT) {
            throw new IllegalStateException("인증 시도 횟수를 초과했습니다.");
        }

        if (!savedCode.equals(code)) {
            VerificationState next =
                    new VerificationState(
                            state.getAttemptCount() + 1,
                            state.getResendBlockedUntil()
                    );

            redisTemplate.opsForValue().set(
                    metaKey(email),
                    next.toValue(),
                    CODE_TTL_MINUTES,
                    TimeUnit.MINUTES
            );

            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        // 4️⃣ 인증 성공 처리
        redisTemplate.opsForValue().set(
                verifiedKey(email),
                "true",
                VERIFIED_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        redisTemplate.delete(codeKey(email));
        redisTemplate.delete(metaKey(email));
    }

    @Override
    public boolean isVerified(String email) {
        return "true".equals(
                redisTemplate.opsForValue().get(verifiedKey(email))
        );
    }
    
    @Override
    public void clear(String email) {
        redisTemplate.delete(verifiedKey(email));
    }
}