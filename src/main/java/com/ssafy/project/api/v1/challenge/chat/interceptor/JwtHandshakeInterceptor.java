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
        // 1️⃣ 쿼리 파라미터에서 token 꺼내기
        String query = request.getURI().getQuery(); // token=xxx
        String token = null;

        if (query != null && query.startsWith("token=")) {
            token = query.substring(6);
        }

        if (token != null) {
            try {
                Claims claims = jwtUtil.getClaims(token);

                Long userId = claims.get("userId", Long.class);
                String loginId = claims.get("loginId", String.class);
                String nickname = claims.get("nickname", String.class);

                UserPrincipal principal =
                        new UserPrincipal(userId, loginId, nickname);

                log.info("[WS] principal 저장 성공: {}", principal);
                attributes.put("principal", principal);

            } catch (Exception e) {
                log.warn("[WS] 토큰 검증 실패");
            }
        }

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
