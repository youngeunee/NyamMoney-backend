package com.ssafy.project.api.v1.user.service;

import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;

public interface UserService {
	UserDto signup(UserSignupRequest req);
}
