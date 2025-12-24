package com.ssafy.project.api.v1.challenge.chat.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @MessageMapping("/challenges/chat")
    public void receiveAndBroadcast(ChallengeChatMessage message, SimpMessageHeaderAccessor accessor) {
    	 log.info("🔥🔥🔥 CHAT MESSAGE RECEIVED: {}", message);

        // 1. HTTP 인증 컨텍스트에서 사용자 꺼내기
    	 Object principalObj = accessor.getSessionAttributes().get("principal");
        
        log.info("🔍 principalObj = {}", principalObj);

        if (!(principalObj instanceof UserPrincipal)) {
            // 인증 안 된 사용자 무시
            return;
        }

        UserPrincipal principal = (UserPrincipal) principalObj;

        Long userId = principal.getUserId();
        String nickname = principal.getNickname();

        // 2. 참여자 검증
        challengeChatService.validateParticipant(
                message.getChallengeId(),
                userId
        );

        // 3.sender 정보 서버에서 세팅
        message.setSenderId(userId);
        message.setSenderNickname(nickname);
        message.setSentAt(LocalDateTime.now());

        // DB 저장 추가
        challengeChatService.saveMessage(message);
        
        // 4. 브로드캐스트
        messagingTemplate.convertAndSend(
                "/topic/challenges/" + message.getChallengeId(),
                message
        );
    }
}
