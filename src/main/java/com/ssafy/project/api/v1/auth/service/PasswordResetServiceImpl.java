package com.ssafy.project.api.v1.auth.service;

import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.auth.caller.AuthMailCaller;
import com.ssafy.project.api.v1.auth.dto.PasswordResetConfirmRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetSendCodeRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetSendCodeResponse;
import com.ssafy.project.api.v1.auth.dto.PasswordResetVerifyCodeRequest;
import com.ssafy.project.api.v1.auth.dto.PasswordResetVerifyCodeResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.api.v1.auth.dto.VerificationState; // ✅

// 참고: https://velog.io/@viva99/Redis%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%EC%9D%B4%EB%A9%94%EC%9D%BC-%EC%9D%B8%EC%A6%9D 
// 참고2: https://velog.io/@viva99/Redis%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%98%EC%97%AC-%EC%9D%B4%EB%A9%94%EC%9D%BC-%EC%9D%B8%EC%A6%9D 
@Service
public class PasswordResetServiceImpl implements PasswordResetService {
	
	private final AuthMailCaller mailCaller;
	private final StringRedisTemplate redisTemplate;
	private final UserService userService;
	
	private static final String PREFIX = "auth:pwdreset:";
	private static final long CODE_TTL_MINUTES = 10;
	private static final long VERIFIED_TTL_MINUTES = 5;
	private static final long RESEND_COOLDOWN_MS = 60_000;
	private static final int MAX_ATTEMPT = 5;
	
	public PasswordResetServiceImpl(
	        AuthMailCaller mailCaller,
	        StringRedisTemplate redisTemplate,
	        UserService userService
	) {
	    this.mailCaller = mailCaller;
	    this.redisTemplate = redisTemplate;
	    this.userService = userService;
	}

	private String codeKey(String loginId) {
	    return PREFIX + "code:" + loginId;
	}

	private String metaKey(String loginId) {
	    return PREFIX + "meta:" + loginId;
	}

	private String verifiedKey(Long userId) {
	    return PREFIX + "verified:" + userId;
	}

	@Override
	public PasswordResetSendCodeResponse sendVerificationCode(PasswordResetSendCodeRequest req) throws NoSuchAlgorithmException {
	    Long userId = req.getUserId();
	    String loginId = req.getLoginId();
	    String email = req.getEmail();

	    // 1️⃣ 사용자 검증
	    userService.validateUser(userId, loginId, email);

	    // 2️⃣ 재전송 쿨타임 체크
	    long now = System.currentTimeMillis();
	    String metaRedisKey = metaKey(loginId);

	    VerificationState state =
	            VerificationState.from(redisTemplate.opsForValue().get(metaRedisKey));

	    if (state.getResendBlockedUntil() > now) {
	        throw new IllegalStateException("인증번호 재전송 대기 중입니다.");
	    }

	    // 3️⃣ 인증번호 생성 + 메일 발송
	    String code = mailCaller.makeCode();
	    mailCaller.sendPasswordResetCode(email, loginId, code);

	    // 4️⃣ Redis 저장 (code는 hash로)
	    String codeHash = hash(code);
	    redisTemplate.opsForValue()
	            .set(codeKey(loginId), codeHash, CODE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);

	    long blockedUntil = now + RESEND_COOLDOWN_MS;
	    VerificationState newState = new VerificationState(0, blockedUntil);

	    redisTemplate.opsForValue()
	            .set(metaRedisKey, newState.toValue(), CODE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);

	    return PasswordResetSendCodeResponse.builder()
	            .resendAvailableAt(blockedUntil)
	            .expiresInSeconds((int) (CODE_TTL_MINUTES * 60))
	            .build();
	}

	@Override
	public PasswordResetVerifyCodeResponse checkVerificationCode(PasswordResetVerifyCodeRequest req) throws NoSuchAlgorithmException {
	    Long userId = req.getUserId();
	    String loginId = req.getLoginId();
	    String inputCode = req.getVerificationCode();

	    // 1️⃣ 코드 조회
	    String savedHash = redisTemplate.opsForValue().get(codeKey(loginId));
	    if (savedHash == null) {
	        throw new IllegalStateException("인증번호가 만료되었습니다.");
	    }

	    // 2️⃣ 상태 조회
	    String metaRedisKey = metaKey(loginId);
	    VerificationState state =
	            VerificationState.from(redisTemplate.opsForValue().get(metaRedisKey));

	    if (state.getAttemptCount() >= MAX_ATTEMPT) {
	        throw new IllegalStateException("인증 시도 횟수를 초과했습니다.");
	    }

	    // 3️⃣ 코드 비교
	    if (!savedHash.equals(hash(inputCode))) {
	        VerificationState next =
	                new VerificationState(state.getAttemptCount() + 1, state.getResendBlockedUntil());

	        redisTemplate.opsForValue()
	                .set(metaRedisKey, next.toValue(), CODE_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);

	        throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
	    }

	    // 4️⃣ 성공 처리
	    redisTemplate.opsForValue()
	            .set(verifiedKey(userId), "true", VERIFIED_TTL_MINUTES, java.util.concurrent.TimeUnit.MINUTES);

	    redisTemplate.delete(codeKey(loginId));
	    redisTemplate.delete(metaRedisKey);

	    return PasswordResetVerifyCodeResponse.builder()
	            .verified(true)
	            .build();
	}

	@Override
	public void changePassword(PasswordResetConfirmRequest req) {
	    Long userId = req.getUserId();

	    // 1️⃣ 인증 완료 여부 확인
	    String verified = redisTemplate.opsForValue().get(verifiedKey(userId));
	    if (!"true".equals(verified)) {
	        throw new IllegalStateException("인증이 완료되지 않았습니다.");
	    }

	    // 2️⃣ 실제 비밀번호 변경 (userService)
	    userService.resetPassword(
	            userId,
	            req.getNewPassword(),
	            req.getNewPasswordConfirm()
	    );

	    // 3️⃣ 인증 상태 제거
	    redisTemplate.delete(verifiedKey(userId));
	}
	
	private String hash(String raw) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
	    return Base64.getEncoder().encodeToString(dig);
	}
}
