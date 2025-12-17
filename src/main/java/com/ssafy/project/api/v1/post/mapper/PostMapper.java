package com.ssafy.project.api.v1.post.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.post.dto.Post;
import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostDto;

@Mapper
public interface PostMapper {

	//Long createPost(@Param("dto")PostDto dto, @Param("userId")Long userId);

	PostDetailResponse getPostDetail(Long postId);

	int createPost(PostDto dto);

	void updatePost(PostDto dto);

	void deletePost(Long postId);

	List<PostDetailResponse> findPostList(@Param("boardId") Long boardId,
			@Param("offset") int offset,
			@Param("size") int size,
			@Param("sort") String sort,
			@Param("keyword") String keyword);
	
	long countPostList(@Param("boardId") Long boardId, @Param("keyword") String keyword);
	
	// 좋아요 관련
	void increaseLike(Long postId);

	void decreaseLike(Long postId);

	int getLikesCount(Long postId);

	// 게시글 존재?
	Integer existsPost(@Param("boardId") Long boardId, @Param("postId") Long postId);

	Post findById(Long postId);

}