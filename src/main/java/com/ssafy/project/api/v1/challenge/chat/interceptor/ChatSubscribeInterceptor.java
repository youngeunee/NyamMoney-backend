package com.ssafy.project.api.v1.challenge.chat.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.ssafy.project.api.v1.challenge.chat.service.ChallengeChatService;
import com.ssafy.project.api.v1.challenge.service.ChallengeParticipantService;
import com.ssafy.project.security.auth.UserPrincipal;

@Component
public class ChatSubscribeInterceptor implements ChannelInterceptor {
    private final ChallengeChatService challengeChatService;
    public ChatSubscribeInterceptor(ChallengeChatService challengeChatService) {
    	this.challengeChatService = challengeChatService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // SUBSCRIBE만 가로챈다
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            // 1) WebSocket 세션에서 principal 꺼내기
            Object principalObj = accessor.getSessionAttributes().get("principal");
            if (!(principalObj instanceof UserPrincipal)) {
                // 인증 안 된 사용자
                throw new RuntimeException("인증되지 않은 사용자입니다.");
            }

            UserPrincipal principal = (UserPrincipal) principalObj;
            Long userId = principal.getUserId();

            // 2) destination 파싱: /topic/challenges/{challengeId}
            String destination = accessor.getDestination();
            Long challengeId = extractChallengeId(destination);

            // 3) 참여자 검증 (JOINED)
            challengeChatService.validateParticipant(challengeId, userId);
        }

        return message;
    }
    
    /**
     * destination에서 challengeId 추출
     * 예: /topic/challenges/3 -> 3
     */
    private Long extractChallengeId(String destination) {
        if (destination == null) {
            throw new RuntimeException("destination이 없습니다.");
        }

        // 안전하게 분해
        String[] parts = destination.split("/");
        // ["", "topic", "challenges", "{id}"]

        if (parts.length < 4) {
            throw new RuntimeException("잘못된 구독 경로입니다.");
        }

        try {
            return Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("challengeId 파싱 실패");
        }
    }
}
