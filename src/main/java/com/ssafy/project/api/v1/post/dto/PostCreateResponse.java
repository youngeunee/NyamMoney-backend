package com.ssafy.project.api.v1.post.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostCreateResponse {
	private Long postId;
	private Long boardId;
	private String title;
	private LocalDateTime createdAt;

}
