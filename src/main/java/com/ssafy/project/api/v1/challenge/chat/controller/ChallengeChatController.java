package com.ssafy.project.api.v1.challenge.chat.controller;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChallengeChatController {

    private static final Logger log =
            LoggerFactory.getLogger(ChallengeChatController.class);

    private final SimpMessagingTemplate messagingTemplate;

    public ChallengeChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        log.info("ChallengeChatController loaded");
    }

    /**
     * 클라이언트 → 서버 → 같은 챌린지 채팅방으로 브로드캐스트
     *
     * SEND /app/challenges/chat
     * SUBSCRIBE /topic/challenges/{challengeId}
     */
    @MessageMapping("/challenges/chat")
    public void receiveAndBroadcast(ChallengeChatMessage message) {

        log.info("===== 챌린지 채팅 메시지 수신 =====");
        log.info("challengeId = {}", message.getChallengeId());
        log.info("senderId    = {}", message.getSenderId());
        log.info("content     = {}", message.getContent());

        // (아직 DB 저장, 권한 검증 안 함)

        String topic = "/topic/challenges/" + message.getChallengeId();

        // 같은 챌린지 채팅방 구독자에게 메시지 전송
        messagingTemplate.convertAndSend(topic, message);

        log.info("메시지 브로드캐스트 완료 -> {}", topic);
    }
}
