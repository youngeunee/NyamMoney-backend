package com.ssafy.project.api.v1.challenge.chat.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;
import com.ssafy.project.api.v1.challenge.chat.service.ChallengeChatService;
import com.ssafy.project.security.auth.UserPrincipal;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ChallengeChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChallengeChatService challengeChatService;
    public ChallengeChatController(SimpMessagingTemplate messagingTemplate, ChallengeChatService challengeChatService) {
        this.messagingTemplate = messagingTemplate;
        this.challengeChatService = challengeChatService;
    }

    /**
     * 클라이언트 → 서버 → 같은 챌린지 채팅방으로 브로드캐스트
     *
     * SEND /app/challenges/chat
     * SUBSCRIBE /topic/challenges/{challengeId}
     */
    @MessageMapping("/challenges/chat")
    public void receiveAndBroadcast(
            ChallengeChatMessage message,
            SimpMessageHeaderAccessor accessor
    ) {
        UserPrincipal principal =
            (UserPrincipal) accessor
                .getSessionAttributes()
                .get("principal");

        if (principal == null) {
            throw new RuntimeException("인증되지 않은 사용자");
        }

        Long userId = principal.getUserId();

        challengeChatService.validateParticipant(
            message.getChallengeId(),
            userId
        );

        messagingTemplate.convertAndSend(
            "/topic/challenges/" + message.getChallengeId(),
            message
        );
    }

}
