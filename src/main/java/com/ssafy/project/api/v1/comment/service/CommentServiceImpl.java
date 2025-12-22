package com.ssafy.project.api.v1.comment.service;

import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.comment.dto.CommentCreateRequest;
import com.ssafy.project.api.v1.comment.dto.CommentCreateResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentCursorResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDto;
import com.ssafy.project.api.v1.comment.dto.CommentListResponse;
import com.ssafy.project.api.v1.comment.dto.CommentWithPostResponse;
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

	@Override
	public void deleteComment(Long boardId, Long postId, Long commentId, Long userId) throws Exception {
		CommentDetailResponse comment = commentMapper.getComment(postId, commentId);
		if (comment == null) {
	        throw new NotFoundException("존재하지 않는 댓글입니다.");
	    }
	    // 2. 작성자 본인인지 체크
	    if (!comment.getAuthor().getUserId().equals(userId)) {
//	        throw new UnauthorizedException("댓글 삭제 권한이 없습니다.");
	        throw new Exception("댓글 삭제 권한이 없습니다.");
	    }
	    // 3. Soft Delete 실행
	    int updated = commentMapper.deleteComment(commentId);

	    if (updated == 0) {
	        throw new RuntimeException("댓글 삭제에 실패했습니다.");
	    }
	}

	@Override
	public CommentListResponse getCommentList(Long postId, int page, int size) {
		int offset=page*size;
		int totalElements = commentMapper.countComments(postId);
		List<CommentDetailResponse> list = commentMapper.getCommentList(postId, size, offset);
		int totalPages = (int) Math.ceil((double) totalElements/size);
		
		CommentListResponse rsp = new CommentListResponse();
		rsp.setContent(list);
		rsp.setPage(page);
		rsp.setSize(size);
		rsp.setTotalElements(totalElements);
		rsp.setTotalPages(totalPages);
		
		return rsp;
	}

	@Override
	public CommentCursorResponse getUserComments(Long userId, Long cursor, int size) {
		int fetchSize = size + 1;
		// cursor 기반으로 size+1 조회
		List<CommentWithPostResponse> list = commentMapper.selectUserComments(userId, cursor, fetchSize);
		boolean hasNext = false;
		Long nextCursor = null;
		if (list.size() > size) {
			hasNext = true;
			CommentWithPostResponse last = list.remove(list.size() - 1);
			nextCursor = last.getCommentId();
		}

		CommentCursorResponse rsp = new CommentCursorResponse();
		rsp.setItems(list);
		rsp.setHasNext(hasNext);
		rsp.setNextCursor(nextCursor);
		rsp.setTotalCount(commentMapper.countUserComments(userId));
		return rsp;
	}

}
