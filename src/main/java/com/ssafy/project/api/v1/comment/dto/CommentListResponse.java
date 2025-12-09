package com.ssafy.project.api.v1.comment.dto;

import java.util.List;

import lombok.Data;

@Data
public class CommentListResponse {
	private List<CommentDetailResponse> content;

    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
    
}