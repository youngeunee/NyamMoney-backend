package com.ssafy.project.api.v1.comment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
public class CommentWithPostResponse {
	private Long commentId;
	private Long postId;
	private Long boardId;
	private String boardName;
	private String postTitle;
	private String content;
	private String createdAt;
	private String updatedAt;
	private Integer likeCount;
	private Integer commentCount;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long authorUserId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String authorNickname;
}
