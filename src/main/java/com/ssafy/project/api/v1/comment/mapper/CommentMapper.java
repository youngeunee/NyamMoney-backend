package com.ssafy.project.api.v1.comment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDto;


@Mapper
public interface CommentMapper {

	CommentDetailResponse getComment(@Param("postId")Long postId, @Param("commentId")Long commentId);

	void createComment(CommentDto dto);

	int updateComment(Long commentId, String content);

}
