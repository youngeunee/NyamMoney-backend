package com.ssafy.project.api.v1.challenge.chat.controller;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.ssafy.project.api.v1.challenge.chat.dto.ChallengeChatMessage;
import com.ssafy.project.api.v1.challenge.chat.service.ChallengeChatService;
import com.ssafy.project.security.auth.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeChatQueryController {
    private final ChallengeChatService challengeChatService;
    public ChallengeChatQueryController(ChallengeChatService challengeChatService) {
    	this.challengeChatService = challengeChatService;
    	
    }

    @GetMapping("/{challengeId}/chats")
    public List<ChallengeChatMessage> getChatHistory(
            @PathVariable Long challengeId
    ) {
        UserPrincipal principal =
            (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Long userId = principal.getUserId();

        return challengeChatService.getChatHistory(challengeId, userId);
    }
}
