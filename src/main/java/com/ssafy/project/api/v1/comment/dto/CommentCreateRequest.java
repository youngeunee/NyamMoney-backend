package com.ssafy.project.api.v1.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
	@NotBlank
	private String content;

}
