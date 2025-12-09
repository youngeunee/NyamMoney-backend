package com.ssafy.project.api.v1.comment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class CommentDetailResponse {
	private Long commentId;
    private Long postId;
    private String content;

    private AuthorInfo author;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorInfo {
        private Long userId;
        private String nickname;
    }
	

}
