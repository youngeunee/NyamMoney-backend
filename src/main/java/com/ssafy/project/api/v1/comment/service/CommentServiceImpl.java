package com.ssafy.project.api.v1.comment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.comment.dto.CommentCreateRequest;
import com.ssafy.project.api.v1.comment.dto.CommentCreateResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDto;
import com.ssafy.project.api.v1.comment.dto.CommentUpdateRequest;
import com.ssafy.project.api.v1.comment.mapper.CommentMapper;

@Service
public class CommentServiceImpl implements CommentService {
	private final CommentMapper commentMapper;
	public CommentServiceImpl(CommentMapper commentMapper) {
		this.commentMapper = commentMapper;
	}

	@Override
	public CommentDetailResponse getComment(Long boardId, Long postId, Long commentId) {
		return commentMapper.getComment(postId, commentId);
	}

	@Override
	public CommentCreateResponse createComment(Long postId, Long userId, CommentCreateRequest req) {
		CommentDto dto = new CommentDto();
		dto.setPostId(postId);
		dto.setUserId(userId);
		dto.setContentMd(req.getContent());
		
		commentMapper.createComment(dto);
		return new CommentCreateResponse(dto.getCommentId());
	}

	@Override
	@Transactional
	public CommentDetailResponse updateComment(Long postId, Long commentId, CommentUpdateRequest req, Long userId) {
		int updated = commentMapper.updateComment(commentId, req.getContent());
        if (updated == 0) {
            throw new RuntimeException("수정할 댓글이 없거나 이미 삭제됨");
        }

        CommentDetailResponse dto = commentMapper.getComment(postId, commentId);
        if (dto == null) {
            throw new RuntimeException("댓글 조회 실패");
        }
        
		return dto;
	}

}
