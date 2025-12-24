package com.ssafy.project.api.v1.auth.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.auth.dto.TokenRefreshResponse;
import com.ssafy.project.api.v1.auth.service.AuthService;
import com.ssafy.project.api.v1.user.dto.UserLoginRequest;
import com.ssafy.project.api.v1.user.dto.UserLoginResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final UserService uService;
	private final AuthService aService;
	private final JWTUtil jwtUtil;
	
	public AuthController(UserService uService, AuthService aService, JWTUtil jwtUtil) {
        this.uService = uService;
        this.aService = aService;
        this.jwtUtil = jwtUtil;
    }
	
	@SecurityRequirement(name = "")
	@PostMapping("login")
	public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest req){
		UserLoginResponse res = uService.login(req);

        ResponseCookie accessCookie = buildCookie(
                "accessToken",
                res.getAccessToken(),
                jwtUtil.getClaims(res.getAccessToken()).getExpiration().getTime());

        ResponseCookie refreshCookie = buildCookie(
                "refreshToken",
                res.getRefreshToken(),
                jwtUtil.getClaims(res.getRefreshToken()).getExpiration().getTime());
		
		return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString(), refreshCookie.toString())
                .body(res);
	}
	
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        TokenRefreshResponse res = aService.refresh(refreshToken);

        ResponseCookie accessCookie = buildCookie(
                "accessToken",
                res.getAccessToken(),
                jwtUtil.getClaims(res.getAccessToken()).getExpiration().getTime());

        ResponseCookie refreshCookie = res.getRefreshToken() != null
                ? buildCookie("refreshToken", res.getRefreshToken(),
                              jwtUtil.getClaims(res.getRefreshToken()).getExpiration().getTime())
                : null;

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString());

        if (refreshCookie != null) {
            builder.header(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return builder.body(res);
    }
	
	@Operation(
		    summary = "搿滉犯?勳泝",
		    description = "?勳灛 搿滉犯?疙暅 ?毄?愳潣 refresh token????牅?╇媹??",
		    security = { @SecurityRequirement(name = "bearerAuth") }
		)
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest req ){
		String accessToken = extractAccessToken(req);
		if (accessToken == null) {
            throw new IllegalAccessError("?犿毃??access token???勲嫏?堧嫟.");
        }

        Claims claims = jwtUtil.getClaims(accessToken);
        Long userId = claims.get("userId", Long.class);
		
		aService.logout(userId);

        ResponseCookie expiredAccess = expireCookie("accessToken");
        ResponseCookie expiredRefresh = expireCookie("refreshToken");
		
		return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccess.toString(), expiredRefresh.toString())
                .body(Map.of("message", "搿滉犯?勳泝 ?橃叏?惦媹??"));
	}

    private String extractAccessToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        if (req.getCookies() != null) {
            for (var c : req.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    private ResponseCookie buildCookie(String name, String value, long expiresAtMillis) {
        long maxAgeSeconds = Math.max(1, (expiresAtMillis - System.currentTimeMillis()) / 1000);
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false) // TODO: 배포 환경에서는 true 로 전환
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    private ResponseCookie expireCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
