package com.ssafy.project.api.v1.user.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.auth.refreshToken.dto.RefreshTokenDto;
import com.ssafy.project.api.v1.auth.refreshToken.mapper.RefreshTokenMapper;
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
import com.ssafy.project.security.jwt.JWTUtil;

@Service
public class UserServiceImpl implements UserService {
	private final UserMapper uMapper;
	private final PasswordEncoder passwordEncoder;
	private final JWTUtil jwtUtil;
	private final RefreshTokenMapper rMapper;
	
	private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
	
	public UserServiceImpl(UserMapper uMapper, PasswordEncoder passwordEncoder, JWTUtil jwtUtil, RefreshTokenMapper rMapper) {
		this.uMapper = uMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.rMapper = rMapper;
	}
	
	@Override
	public UserDto signup(UserSignupRequest req) {
		// 비밀번호 일치 여부 확인
		if(!req.getPassword().equals(req.getPasswordConfirm())) {
			throw new IllegalArgumentException("PASSWORD_MISMATCH");
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
        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);
        
        // 토큰 만료 시간 추출 
        Date refreshExp = jwtUtil.getClaims(refreshToken).getExpiration();
        LocalDateTime expiresAt = refreshExp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        
        // db에 refresh token 저장 
        RefreshTokenDto refreshTokenDto = RefreshTokenDto.builder()
                .userId(user.getUserId())
                .tokenHash(refreshToken)     // 일단 해싱 안하고 바로 저장  
                .expiresAt(expiresAt)
                .build();

        rMapper.insertRefreshToken(refreshTokenDto);
        
		return new UserLoginResponse(user.getUserId(), user.getLoginId(), user.getNickname(), accessToken, refreshToken, user.getRole(), user.getName());
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
	            cursorPostId = c.postId();
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

}
