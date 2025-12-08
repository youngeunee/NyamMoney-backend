package com.ssafy.project.api.v1.user.service;

import java.net.Authenticator.RequestorType;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.dto.UserPasswordUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateResponse;
import com.ssafy.project.api.v1.user.mapper.UserMapper;

@Service
public class UserServiceImpl implements UserService {
	private final UserMapper uMapper;
	private final PasswordEncoder passwordEncoder;
	
	public UserServiceImpl(UserMapper uMapper, PasswordEncoder passwordEncoder) {
		this.uMapper = uMapper;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Override
	public UserDto signup(UserSignupRequest req) {
		// 비밀번호 일치 여부 확인
		if(!req.getPassword().equals(req.getPasswordConfirm())) {
			return null; // 나중에 error로 바꾸기
		}
		
		String hashedPw = passwordEncoder.encode(req.getPassword());
		UserDto user = UserDto.builder()
		        .loginId(req.getLoginId())
		        .pwHash(hashedPw) // 비밀번호 해시처리해서 넣기 -> 나중에 추가
		        .nickname(req.getNickname())
		        .email(req.getEmail())
		        .monthlyBudget(req.getMonthlyBudget())
		        .triggerBudget(req.getTriggerBudget())
		        .profileVisibility(req.getProfileVisibility())
		        .shareLevel(req.getShareLevel())
		        .build();
		
		try {
	        uMapper.insertUser(user);
	    } catch (DuplicateKeyException e) {
	        // 중복 관련 예외 → 프론트에게 전달
	       return null; // 나중에 에러로 바꾸기
	    }
		
		return user;
	}

	@Override
	public UserLoginResponse login(UserLoginRequest req) {
		UserDto user = uMapper.findByLoginId(req.getLoginId());
		if(user == null) {
			throw new IllegalArgumentException("아이디/비밀번호가 올바르지 않습니다.");
		}
		
		boolean match = passwordEncoder.matches(req.getPassword(), user.getPwHash());
		
		if(!match) throw new IllegalArgumentException("아이디/비밀번호가 올바르지 않습니다.");
		
		return new UserLoginResponse(user.getUserId(), user.getLoginId(), user.getNickname());
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
                user.getShareLevel()
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
                    user.getShareLevel()
                    
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
                updated.getShareLevel()
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

}
