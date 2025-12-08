package com.ssafy.project.api.v1.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostCreateRequest {
	@NotNull
	private String title;
	@NotNull
	private String contentMd;
	
	private Long challengeId;

}
