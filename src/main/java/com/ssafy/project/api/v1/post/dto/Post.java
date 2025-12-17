package com.ssafy.project.api.v1.post.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Post {
	private Long PpostId;
	private Long userId;
	private LocalDateTime deletedAt;

}
