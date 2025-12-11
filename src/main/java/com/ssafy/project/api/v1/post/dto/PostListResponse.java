package com.ssafy.project.api.v1.post.dto;

import java.util.List;

import lombok.Data;

@Data
public class PostListResponse {
	private List<PostDetailResponse> content;
	private int page;
	private int size;
	private int totalElements;
	private int totalPages;

}
