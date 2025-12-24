package com.ssafy.project.api.v1.challenge.chat.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
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
        String userIdHeader = request.getHeaders().getFirst("userId");
        log.info("[WS] CONNECT userId={}", userIdHeader);

        if (userIdHeader != null) {
            try {
                Long userId = Long.valueOf(userIdHeader);

                // 1 Redis로 로그인 여부 확인
                redisAuthService.validateLogin(userId);

                // 2 기존 UserService 재사용
                UserDetailResponse user =
                        userService.getUserDetail(userId);

                // 3 WebSocket용 Principal 생성
                UserPrincipal principal = new UserPrincipal(
                        user.getUserId(),
                        user.getLoginId(),
                        user.getNickname()
                );

                attributes.put("principal", principal);

                log.info("[WS] 인증 성공 principal={}", principal);

            } catch (Exception e) {
                log.warn("[WS] 인증 실패: {}", e.getMessage());
            }
        }

        // 연결 자체는 허용
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {}
}
