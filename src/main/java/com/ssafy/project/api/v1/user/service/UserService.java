package com.ssafy.project.api.v1.user.service;

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
import com.ssafy.project.common.dto.CursorPage;

public interface UserService {
	UserDto signup(UserSignupRequest req);
	
	UserLoginResponse login(UserLoginRequest req);
	
	UserDetailResponse getUserDetail(Long userId);
	
	UserUpdateResponse updateUser(Long userId, UserUpdateRequest req);

	void deleteUser(Long userId);
	
    void updatePassword(Long userId, UserPasswordUpdateRequest req);

	boolean checkNickname(String nickname);

	boolean checkLoginId(String loginId);

	CursorPage<UserPostItem> getUserPosts(Long userId, UserPostCursorRequest request);

	boolean checkEmail(String email);
}
