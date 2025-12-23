package com.ssafy.project.api.v1.challenge.chat.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeChatMessage {
	private Long challengeId;
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;

}
