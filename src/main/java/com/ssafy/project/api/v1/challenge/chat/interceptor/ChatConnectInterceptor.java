package com.ssafy.project.api.v1.challenge.chat.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.ssafy.project.security.auth.UserPrincipal;
import com.ssafy.project.security.jwt.JWTUtil;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChatConnectInterceptor implements ChannelInterceptor {
	private final JWTUtil jwtUtil;

    public ChatConnectInterceptor(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            log.info("[WS-CONNECT] Authorization = {}", authHeader);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Claims claims = jwtUtil.getClaims(token);

                UserPrincipal principal = new UserPrincipal(
                        claims.get("userId", Long.class),
                        claims.get("loginId", String.class),
                        claims.get("nickname", String.class)
                );

                accessor.getSessionAttributes().put("principal", principal);
                log.info("[WS-CONNECT] principal 저장 완료: {}", principal);
            }
        }

        return message;
    }
}
