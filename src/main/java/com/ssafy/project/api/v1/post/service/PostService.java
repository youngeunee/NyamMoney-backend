package com.ssafy.project.api.v1.post.service;

import com.ssafy.project.api.v1.post.dto.PostCreateRequest;
import com.ssafy.project.api.v1.post.dto.PostCreateResponse;
import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostDto;
import com.ssafy.project.api.v1.post.dto.PostListResponse;
import com.ssafy.project.api.v1.post.dto.PostUpdateRequest;

import jakarta.validation.Valid;

public interface PostService {

	// Long createPost(PostDto dto, Long userId);

	PostDetailResponse getPostDetail(Long postId);

	PostCreateResponse createPost(Long boardId, Long userId, @Valid PostCreateRequest dto);

	PostDetailResponse updatePost(Long postId, Long userId, @Valid PostUpdateRequest req);

	void deletePost(Long boardId, Long postId, Long userId);

	PostListResponse getPostList(Long boardId, int page, int size, String sort, String keyword);

}
