package com.ssafy.project.api.v1.comment.service;

import org.apache.ibatis.javassist.NotFoundException;

import com.ssafy.project.api.v1.comment.dto.CommentCreateRequest;
import com.ssafy.project.api.v1.comment.dto.CommentCreateResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentCursorResponse;
import com.ssafy.project.api.v1.comment.dto.CommentListResponse;
import com.ssafy.project.api.v1.comment.dto.CommentUpdateRequest;

public interface CommentService {

	CommentDetailResponse getComment(Long boardId, Long postId, Long commentId);

	CommentCreateResponse createComment(Long postId, Long userId, CommentCreateRequest req);

	CommentDetailResponse updateComment(Long postId, Long commentId, CommentUpdateRequest req, Long userId);

	void deleteComment(Long boardId, Long postId, Long commentId, Long userId) throws NotFoundException, Exception;

	CommentListResponse getCommentList(Long postId, int page, int size);

	CommentCursorResponse getUserComments(Long userId, Long cursor, int size);

}
