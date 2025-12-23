package com.ssafy.project.api.v1.challenge.chat.interceptor;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.ssafy.project.security.auth.UserPrincipal;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JWTUtil jwtUtil;
    public JwtHandshakeInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // Authorization: Bearer xxx
        String authHeader = request.getHeaders().getFirst("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 🔥 REST와 동일한 로직
                Claims claims = jwtUtil.getClaims(token);

                Long userId = claims.get("userId", Long.class);
                String loginId = claims.get("loginId", String.class);
                String nickname = claims.get("nickname", String.class);

                UserPrincipal principal =
                        new UserPrincipal(userId, loginId, nickname);
                log.info("[WS] principal 저장: {}", principal);

                // WebSocket 세션에 저장
                attributes.put("principal", principal);

            } catch (Exception e) {
                // 토큰 문제 있으면 principal 저장 안 함
                // (연결은 허용, SEND/SUBSCRIBE에서 차단)
            }
        }

        // 토큰 없거나 잘못되면 연결은 허용(읽기 전용)
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }
}
