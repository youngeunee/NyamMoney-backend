package com.ssafy.project.api.v1.post.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostDetailResponse {
	private Long postId;
	private Long boardId;
	private String title;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private int commentCount;
	private int likeCount;
	
	private AuthorInfo author;
	
	@Data
	public static class AuthorInfo {
		private Long userId;
		private String nickname;
	}
	
	
}
