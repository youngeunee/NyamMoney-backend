package com.ssafy.project.api.v1.user.service;

import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;

public class UserServiceImpl implements UserService {

	@Override
	public UserDto signup(UserSignupRequest req) {
		// 비밀번호 일치 여부 확인
		if(!req.getPassword().equals(req.getPasswordConfirm())) {
			return null; // 나중에 error로 바꾸기
		}
		
		UserDto user = UserDto.builder()
		        .loginId(req.getLoginId())
		        .pwHash(hashedPw) // 비밀번호 해시처리해서 넣기 -> 나중에 추가
		        .nickname(req.getNickname())
		        .email(req.getEmail())
		        .monthlyBudget(req.getMonthlyBudget())
		        .triggerBudget(req.getTriggerBudget())
		        .build();
		
		try {
	        uMapper.insertUser(user);
	    } catch (DuplicateKeyException e) {
	        // 중복 관련 예외 → 프론트에게 전달
	        throw new Exception(); // 나중에 에러로 바꾸기
	    }
		
		return user;
	}

}
