package com.ssafy.project.api.v1.challenge.chat.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.security.auth.UserPrincipal;
import com.ssafy.project.security.redis.RedisAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    private final RedisAuthService redisAuthService;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        try {
            // 1️ HTTP Cookie에서 accessToken 추출
            String accessToken = extractAccessTokenFromCookie(request);
            if (accessToken == null) {
                log.warn("[WS] accessToken 없음");
                return true;
            }

            // 2️ JWT 파싱 → userId
            Long userId = redisAuthService.extractUserIdFromAccessToken(accessToken);

            // 3️ Redis 로그인 상태 검증
            redisAuthService.validateLogin(userId);

            // 4️ 기존 UserService 재사용
            UserDetailResponse user = userService.getUserDetail(userId);

            // 5️ WebSocket 전용 Principal 생성
            UserPrincipal principal = new UserPrincipal(
                    user.getUserId(),
                    user.getLoginId(),
                    user.getNickname()
            );

            // 6️ 세션에 저장
            attributes.put("principal", principal);

            log.info("[WS] 인증 성공 principal={}", principal);

        } catch (Exception e) {
            log.warn("[WS] 인증 실패: {}", e.getMessage());
        }

        return true; // 연결 자체는 허용 (SEND/SUBSCRIBE에서 차단)
    }

    private String extractAccessTokenFromCookie(ServerHttpRequest request) {
        if (request.getHeaders().get("Cookie") == null) return null;

        return request.getHeaders().get("Cookie").stream()
                .flatMap(c -> java.util.Arrays.stream(c.split(";")))
                .map(String::trim)
                .filter(c -> c.startsWith("accessToken="))
                .map(c -> c.substring("accessToken=".length()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {}
}

