package com.ssafy.project.api.v1.challenge.chat.controller;

import java.time.LocalDateTime;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public void receiveAndBroadcast(
            ChallengeChatMessage message,
            SimpMessageHeaderAccessor accessor
    ) {
        UserPrincipal principal =
            (UserPrincipal) accessor
                .getSessionAttributes()
                .get("principal");
//        UserPrincipal user;
//        Long userId = user.getUserId();

        if (principal == null) {
        	throw new RuntimeException("인증되지 않은 사용자");
        }

        Long userId = principal.getUserId();
        String nickname = principal.getNickname();
        log.debug("유저아이디 =-======", userId);
        // 참여자 검증
        challengeChatService.validateParticipant(
            message.getChallengeId(),
            userId
            
        );

        // 서버에서 sender 정보 세팅
        message.setSenderId(userId);
        message.setSenderNickname(nickname);
        message.setSentAt(LocalDateTime.now());

        // DB 저장
        challengeChatService.saveMessage(message);

        // 브로드캐스트
        messagingTemplate.convertAndSend(
            "/topic/challenges/" + message.getChallengeId(),
            message
        );
    }

}
