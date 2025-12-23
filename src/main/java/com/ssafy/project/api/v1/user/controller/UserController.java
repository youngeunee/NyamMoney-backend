package com.ssafy.project.api.v1.user.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.user.dto.DuplicateCheckResponse;
import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.dto.UserDto;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.dto.UserPasswordUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserPostCursorRequest;
import com.ssafy.project.api.v1.user.dto.UserPostItem;
import com.ssafy.project.api.v1.user.dto.UserSignupRequest;
import com.ssafy.project.api.v1.user.dto.UserSignupResponse;
import com.ssafy.project.api.v1.user.dto.UserUpdateRequest;
import com.ssafy.project.api.v1.user.dto.UserUpdateResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.common.dto.CursorPage;
import com.ssafy.project.security.auth.UserPrincipal;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
	
	private final UserService uService;
	
	public UserController(UserService uService){
		this.uService = uService;
	}
	
	@PostMapping("/signup")
	public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest req){
		UserDto user = uService.signup(req);
		
		UserSignupResponse res = new UserSignupResponse(user.getUserId(), user.getNickname(), user.getCreatedAt(), user.getProfileVisibility(), user.getShareLevel(), user.getRole(), user.getName());
		
		return ResponseEntity.ok(res);
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDetailResponse> getUserDetail(@PathVariable Long userId) {
		UserDetailResponse res = uService.getUserDetail(userId);
		return ResponseEntity.ok(res);
	}
	
	@PatchMapping("/{userId}")
	public ResponseEntity<UserUpdateResponse> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest req) {
        UserUpdateResponse res = uService.updateUser(userId, req);
        return ResponseEntity.ok(res);
    }
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<?> deleteUser(@PathVariable Long userId){
		uService.deleteUser(userId);
		return ResponseEntity.ok(Map.of("message", "탈퇴되었습니다."));
	}
	
	@PatchMapping("/{userId}/password")
	public ResponseEntity<Map<String, String>> updatePassword(@PathVariable Long userId, @RequestBody UserPasswordUpdateRequest req){
		uService.updatePassword(userId, req);
		return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
	}
	
	@GetMapping("/check-nickname")
	public DuplicateCheckResponse checkNickname(@RequestParam String nickname) {
	    boolean exists = uService.checkNickname(nickname);

	    return new DuplicateCheckResponse(
	            !exists,        // available = 사용 가능 여부 (exists의 반대)
	            "nickname",
	            nickname
	    );
	}
	
	@GetMapping("/check-loginId")
	public DuplicateCheckResponse checkLoginId(@RequestParam String loginId) {
	    boolean exists = uService.checkLoginId(loginId);

	    return new DuplicateCheckResponse(
	            !exists,
	            "loginId",
	            loginId
	    );
	}
	
	@GetMapping("/check-email")
	public DuplicateCheckResponse checkEmail(@RequestParam String email) {
		boolean exists = uService.checkEmail(email);
		
		return new DuplicateCheckResponse(
				!exists,
				"email", email
		);
	}
	
	@GetMapping("/me")
	public ResponseEntity<UserDetailResponse> getMyDetail(@AuthenticationPrincipal UserPrincipal principal) {
		Long userId = principal.getUserId();
		UserDetailResponse response = uService.getUserDetail(userId);
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/{userId}/posts")
	public ResponseEntity<CursorPage<UserPostItem>> getUserPosts(@PathVariable Long userId, @ModelAttribute UserPostCursorRequest request) {
		
		CursorPage<UserPostItem> res = uService.getUserPosts(userId, request);
		
		return ResponseEntity.ok(res);
	}

}
