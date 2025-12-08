package com.ssafy.project.api.v1.post.dto;

import lombok.Data;

@Data
public class PostUpdateRequest {
	
	private String title; // 변경할 제목
	private String contentMd; // 변경할 내용

}
