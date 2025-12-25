package com.ssafy.project.api.v1.user.service;

import java.time.Duration;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;


import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.RedisOperations;

import org.springframework.dao.DuplicateKeyException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.auth.service.AuthService;
import com.ssafy.project.api.v1.auth.service.SignupEmailVerificationService;
import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.dto.UserPasswordUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserPostCursorRequest;
import com.ssafy.project.api.v1.user.dto.UserPostItem;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateResponse;
import com.ssafy.project.api.v1.user.mapper.UserMapper;
import com.ssafy.project.common.dto.CursorPage;
import com.ssafy.project.common.util.CursorUtil;
import com.ssafy.project.common.util.PostExcerptUtil;
import com.ssafy.project.domain.user.model.Role;
import com.ssafy.project.redis.repository.RefreshTokenRepository;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;

@Service
public class UserServiceImpl implements UserService {
	private final UserMapper uMapper;
	private final PasswordEncoder passwordEncoder;
	private final JWTUtil jwtUtil;
//	private final RefreshTokenMapper rMapper;
	private final AuthService authService;
	private final RefreshTokenRepository rMapper;
	private final StringRedisTemplate redisTemplate;
	
	private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    
	public UserServiceImpl(UserMapper uMapper, 
			PasswordEncoder passwordEncoder, 
			JWTUtil jwtUtil, 
			RefreshTokenRepository rMapper, 
			StringRedisTemplate redisTemplate, 
			AuthService authService) {
		
		this.uMapper = uMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.rMapper = rMapper;
		this.redisTemplate = redisTemplate;
		this.authService = authService;
	}
	
	@Override
	public UserDto signup(UserSignupRequest req) {
		// 비밀번호 일치 여부 확인
		if(!req.getPassword().equals(req.getPasswordConfirm())) {
			throw new IllegalArgumentException("PASSWORD_MISMATCH");
		}
		
		if (!authService.isVerified(req.getEmail())) {
		    throw new IllegalStateException("이메일 인증이 필요합니다.");
		}
		
		String hashedPw = passwordEncoder.encode(req.getPassword());
		UserDto user = UserDto.builder()
		        .loginId(req.getLoginId())
		        .pwHash(hashedPw)
		        .nickname(req.getNickname())
		        .email(req.getEmail())
		        .monthlyBudget(req.getMonthlyBudget())
		        .triggerBudget(req.getTriggerBudget())
		        .profileVisibility(req.getProfileVisibility())
		        .shareLevel(req.getShareLevel())
		        .name(req.getName())
		        .phoneNumber(req.getPhoneNumber())
		        .role(Role.USER)
		        .createdAt(LocalDateTime.now())
		        .build();
		try {
	        uMapper.insertUser(user);
	    } catch (DuplicateKeyException e) {
	        // 중복 관련 예외 → 프론트에게 전달
	    	throw new IllegalStateException("DUPLICATE_USER");
	    }
		
		return user;
	}

	@Override
	public UserLoginResponse login(UserLoginRequest req) {
		UserDto user = uMapper.findByLoginId(req.getLoginId());
		// 사용자 조회 
		if(user == null) {
			throw new IllegalArgumentException("아이디/비밀번호가 올바르지 않습니다.");
		}
		
		boolean match = passwordEncoder.matches(req.getPassword(), user.getPwHash());
		
		// 비밀번호 확인 
		if(!match) throw new IllegalArgumentException("아이디/비밀번호가 올바르지 않습니다.");
		
		// 토큰 생성 
//        String accessToken = jwtUtil.createAccessToken(user);
//        String refreshToken = jwtUtil.createRefreshToken(user);
        
//        // 토큰 만료 시간 추출 
//        Date refreshExp = jwtUtil.getClaims(refreshToken).getExpiration();
//        LocalDateTime expiresAt = refreshExp.toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDateTime();
//        
//        // db에 refresh token 저장 
//        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
//                .userId(user.getUserId())
//                .tokenHash(refreshToken)     // 일단 해싱 안하고 바로 저장  
//                .expiresAt(expiresAt)
//                .build();
//
//        rMapper.insertRefreshToken(refreshTokenDto);
        
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        Claims claims = jwtUtil.getClaims(refreshToken);
        String jti = claims.get("jti", String.class);

        String hashKey = "refresh:jti:" + jti;
        String idxKey  = "refresh:user:" + user.getUserId();

        // 해시 저장 값(모두 String)
        Map<String, String> hash = Map.of(
                "userId", String.valueOf(user.getUserId()),
                "loginId", user.getLoginId(),
                "issuedAt", String.valueOf(claims.getIssuedAt().getTime()),
                "expiresAt", String.valueOf(claims.getExpiration().getTime())
        );

        // TTL 계산 (0/음수 방지)
        long ttlMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("refresh token 이 만료되었습니다. 다시 로그인 해주세요.");
        }
        Duration ttl = Duration.ofMillis(ttlMillis);

        // 원자 처리: 기존 전부 삭제 → 새로 1개 등록
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Object execute(RedisOperations operations) {

                // (트랜잭션 전에) 기존 jti 목록을 읽어둠
                Set<String> existing = (Set<String>) operations.opsForSet().members(idxKey);
                if (existing == null) existing = Collections.emptySet();

                operations.multi();

                // 1) 기존 메인키들 삭제
                if (!existing.isEmpty()) {
                    Set<String> oldKeys = existing.stream()
                            .map(oldJti -> "refresh:jti:" + oldJti)
                            .collect(Collectors.toCollection(HashSet::new));
                    operations.delete(oldKeys);
                }

                // 2) 인덱스 Set 교체(단일 정책)
                operations.delete(idxKey);

                // 3) 새 hash 저장 + TTL
                operations.opsForHash().putAll(hashKey, hash);
                operations.expire(hashKey, ttl);

                // 4) 새 jti만 Set에 추가 (+ 선택: idxKey TTL)
                operations.opsForSet().add(idxKey, jti);
                operations.expire(idxKey, ttl);

                return operations.exec();
            }
        });


		return new UserLoginResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                accessToken,
                user.getRole(),
                user.getName(),
                refreshToken);
	}

	@Override
	public UserDetailResponse getUserDetail(Long userId) {
		UserDto user = uMapper.findById(userId);
		if(user == null) throw new IllegalAccessError("해당 사용자를 찾을 수 없습니다");
		
		return new UserDetailResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getNickname(),
                user.getEmail(),
                user.getMonthlyBudget(),
                user.getTriggerBudget(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getProfileVisibility(),
                user.getShareLevel(),
                user.getRole(),
                user.getName(),
                user.getPhoneNumber()
        );
	}

	@Override
	public UserUpdateResponse updateUser(Long userId, UserUpdateRequest req) {
		UserDto user = uMapper.findById(userId);
		
		if(user == null) throw new IllegalAccessError("해당 사용자를 찾을 수 없습니다");
		
		boolean isChanged = false;
		
		if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
            isChanged = true;
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
            isChanged = true;
        }
        if (req.getMonthlyBudget() != null) {
            user.setMonthlyBudget(req.getMonthlyBudget());
            isChanged = true;
        }
        if (req.getTriggerBudget() != null) {
            user.setTriggerBudget(req.getTriggerBudget());
            isChanged = true;
        }
        if (req.getShareLevel() != null) {
            user.setShareLevel(req.getShareLevel());
            isChanged = true;
        }
        if (req.getProfileVisibility() != null) {
            user.setProfileVisibility(req.getProfileVisibility());
            isChanged = true;
        }

        
        if (!isChanged) { // 바뀐거 없으면
            return new UserUpdateResponse(
                    user.getUserId(),
                    user.getLoginId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getMonthlyBudget(),
                    user.getTriggerBudget(),
                    user.getUpdatedAt(),
                    user.getProfileVisibility(),
                    user.getShareLevel(),
                    user.getRole(),
                    user.getName(),
                    user.getPhoneNumber()
            );
        }
        
        uMapper.updateUser(user);
        
        UserDto updated = uMapper.findById(userId);
		
        return new UserUpdateResponse(
                updated.getUserId(),
                updated.getLoginId(),
                updated.getNickname(),
                updated.getEmail(),
                updated.getMonthlyBudget(),
                updated.getTriggerBudget(),
                updated.getUpdatedAt(),
                updated.getProfileVisibility(),
                updated.getShareLevel(),
                updated.getRole(),
                updated.getName(),
                updated.getPhoneNumber()
        );
	}

	@Override
	public void deleteUser(Long userId) {
		UserDto user = uMapper.findById(userId);
		if(user == null) throw new IllegalAccessError("해당 사용자를 찾을 수 없습니다");
		
		int res = uMapper.deleteUser(userId);
		if(res == 0) throw new IllegalAccessError("이미 탈퇴된 사용자입니다");
	}

	@Override
	public void updatePassword(Long userId, UserPasswordUpdateRequest req) {
		UserDto user = uMapper.findById(userId);
		
		if(user == null) throw new IllegalAccessError("해당 사용자를 찾을 수 없습니다");
		
		if(!passwordEncoder.matches(req.getCurrentPassword(), user.getPwHash())) throw new IllegalAccessError("현재 비밀번호가 일치하지 않습니다.");
		
		if(req.getNewPassword() == null || !req.getNewPassword().equals(req.getNewPasswordConfirm())) throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
    
		if (passwordEncoder.matches(req.getNewPassword(), user.getPwHash())) throw new IllegalArgumentException("이전 비밀번호와 다른 비밀번호를 사용해 주세요.");
	     
		String newPwHash = passwordEncoder.encode(req.getNewPassword());
		
		int updated = uMapper.updatePassword(userId, newPwHash);
		
		if(updated == 0) throw new IllegalStateException("비밀번호 변경 실패");
	}

	@Override
	public boolean checkNickname(String nickname) {
		return uMapper.countNickname(nickname) > 0;
	}

	@Override
	public boolean checkLoginId(String loginId) {
		return uMapper.countLoginId(loginId) > 0;
	}
	
	@Override
	public boolean checkEmail(String email) {
		return uMapper.countEmail(email) > 0;
	}
	
	@Override
	public CursorPage<UserPostItem> getUserPosts(Long userId, UserPostCursorRequest req) {

	        // 1) size 결정 (안 들어오면 기본값)
	        int size = req.getSize() == null ? DEFAULT_SIZE : req.getSize();
	        if (size < 1) size = DEFAULT_SIZE;
	        if (size > MAX_SIZE) size = MAX_SIZE;

	        // 2) cursor 파싱 (없으면 null)
	        LocalDateTime cursorCreatedAt = null;
	        Long cursorPostId = null;

	        if (req.getCursor() != null && !req.getCursor().isBlank()) {
	            CursorUtil.Cursor c = CursorUtil.parse(req.getCursor());
	            cursorCreatedAt = c.createdAt();
	            cursorPostId = c.id();
	        }

	        // 3) size + 1 로 조회해서 hasNext 판단
	        List<UserPostItem> rows = uMapper.selectUserPostsCursor(
	                userId,
	                cursorCreatedAt,
	                cursorPostId,
	                size + 1
	        );

	        boolean hasNext = rows.size() > size;
	        if (hasNext) rows = rows.subList(0, size);

	        // 4) excerpt 생성 (목록용)
	        for (UserPostItem item : rows) {
	            String raw = item.getRawContent();
	            item.setExcerpt(PostExcerptUtil.makeExcerpt(raw));
	            item.setRawContent(null); // 제거
	        }

	        // 5) nextCursor 계산 (마지막 요소 기준)
	        String nextCursor = null;
	        if (hasNext && !rows.isEmpty()) {
	            UserPostItem last = rows.get(rows.size() - 1);
	            nextCursor = CursorUtil.format(last.getCreatedAt(), last.getPostId());
	        }
	        
	        long totalCount = uMapper.countUserPosts(userId);
	        return new CursorPage<>(rows, nextCursor, hasNext, totalCount);

	    }
		
	@Override
	public void resetPassword(Long userId, String newPassword, String newPasswordConfirm) {
	    UserDto user = uMapper.findById(userId);

	    if (user == null) throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");

	    if (newPassword == null || !newPassword.equals(newPasswordConfirm)) {
	        throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
	    }

	    if (passwordEncoder.matches(newPassword, user.getPwHash())) {
	        throw new IllegalArgumentException("이전 비밀번호와 다른 비밀번호를 사용해 주세요.");
	    }

	    String newPwHash = passwordEncoder.encode(newPassword);

	    int updated = uMapper.updatePassword(userId, newPwHash);
	    if (updated == 0) throw new IllegalStateException("비밀번호 변경 실패");
	}

	@Override
	public UserDto validateUser(String loginId, String email) {
	    UserDto user = uMapper.findByLoginId(loginId);

	    if (user == null) {
	        throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
	    }

	    if (!user.getEmail().equals(email)) {
	        throw new IllegalArgumentException("이메일 정보가 일치하지 않습니다.");
	    }
	    
	    if(!user.getLoginId().equals(loginId)) {
	        throw new IllegalArgumentException("아이디 정보가 일치하지 않습니다.");
	    }

	    return user;
	}

	@Override
	public UserDto findByLoginId(String loginId) {
	    return uMapper.findByLoginId(loginId);
	}

	@Override
	public void validateSignupEmailAvailable(String email) {
		if(uMapper.countEmail(email) != 0) {
			throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
		}
	}
}
