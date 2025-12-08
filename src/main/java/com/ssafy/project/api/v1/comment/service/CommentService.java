package com.ssafy.project.api.v1.comment.service;

import com.ssafy.project.api.v1.comment.dto.CommentCreateRequest;
import com.ssafy.project.api.v1.comment.dto.CommentCreateResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentUpdateRequest;

public interface CommentService {

	CommentDetailResponse getComment(Long boardId, Long postId, Long commentId);

	CommentCreateResponse createComment(Long postId, Long userId, CommentCreateRequest req);

	CommentDetailResponse updateComment(Long postId, Long commentId, CommentUpdateRequest req, Long userId);

}
