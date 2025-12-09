package com.ssafy.project.api.v1.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.user.dto.UserDto;

@Mapper
public interface UserMapper {

	void insertUser(UserDto user);
	UserDto findByLoginId(String loginId);
	UserDto findById(Long userId);
	int updateUser(UserDto user);
	int deleteUser(Long userId);
	int updatePassword(@Param("userId") Long userId, @Param("pwHash") String pwHash);
	// UserDto findByEmail(String email);
    // UserDto findByNickname(String nickname);
	int countNickname(@Param("nickname") String nickname);
}
