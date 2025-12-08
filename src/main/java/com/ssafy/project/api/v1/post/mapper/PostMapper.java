package com.ssafy.project.api.v1.post.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostDto;

@Mapper
public interface PostMapper {

	//Long createPost(@Param("dto")PostDto dto, @Param("userId")Long userId);

	PostDetailResponse getPostDetail(Long postId);

	int createPost(PostDto dto);

	void updatePost(PostDto dto);

	void deletePost(Long postId);

}
