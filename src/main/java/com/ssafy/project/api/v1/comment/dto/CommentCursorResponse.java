package com.ssafy.project.api.v1.comment.dto;

import java.util.List;

import lombok.Data;

@Data
public class CommentCursorResponse {
	private List<CommentWithPostResponse> items;
	private Long nextCursor;
	private boolean hasNext;
	private long totalCount;
}
