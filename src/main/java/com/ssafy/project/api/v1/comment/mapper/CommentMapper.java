package com.ssafy.project.api.v1.comment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDto;
import com.ssafy.project.api.v1.comment.dto.CommentWithPostResponse;


@Mapper
public interface CommentMapper {

	CommentDetailResponse getComment(@Param("postId")Long postId, @Param("commentId")Long commentId);

	void createComment(CommentDto dto);

	int updateComment(Long commentId, String content);

	int deleteComment(Long commentId);
	
	void softDeleteByPostId(Long postId); // 게시글 삭제시 댓글도 함께 삭

	int countComments(Long postId);

	List<CommentWithPostResponse> selectUserComments(@Param("userId") Long userId,
			@Param("cursor") Long cursor,
			@Param("sizePlusOne") int sizePlusOne);

	long countUserComments(@Param("userId") Long userId);

	List<CommentDetailResponse> getCommentList(@Param("postId")Long postId,
			@Param("limit") int size,
			@Param("offset") int offset);


}
