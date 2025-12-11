package com.ssafy.project.api.v1.post.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.post.dto.PostCreateRequest;
import com.ssafy.project.api.v1.post.dto.PostCreateResponse;
import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostDto;
import com.ssafy.project.api.v1.post.dto.PostListResponse;
import com.ssafy.project.api.v1.post.dto.PostUpdateRequest;
import com.ssafy.project.api.v1.post.mapper.PostMapper;

import jakarta.validation.Valid;

@Service
public class PostServiceImpl implements PostService {
	private final PostMapper postMapper;
	public PostServiceImpl(PostMapper postMapper) {
		this.postMapper = postMapper;
	}

	@Override
	public PostDetailResponse getPostDetail(Long postId) {
		return postMapper.getPostDetail(postId);
	}

	@Override
	public PostCreateResponse createPost(Long boardId, Long userId, @Valid PostCreateRequest req) {
		PostDto dto = new PostDto();
		dto.setBoardId(boardId);
		dto.setUserId(userId);
		dto.setChallengeId(req.getChallengeId());
		dto.setTitle(req.getTitle());
		dto.setContentMd(req.getContentMd());
		
		postMapper.createPost(dto);
		PostDetailResponse detail = postMapper.getPostDetail(dto.getPostId());
		
		return new PostCreateResponse(detail.getPostId(), detail.getBoardId(), detail.getTitle(), detail.getCreatedAt());
	}

	@Override
	public PostDetailResponse updatePost(Long postId, Long userId, @Valid PostUpdateRequest req) {
		PostDto dto = new PostDto();
		dto.setPostId(postId);
		dto.setTitle(req.getTitle());
		dto.setContentMd(req.getContentMd());
		
		postMapper.updatePost(dto);
		
		return postMapper.getPostDetail(postId);
	}

	@Override
	public void deletePost(Long boardId, Long postId, Long userId) {
		// 본인 글인지 or 관리자 권한 체크하려면 로직 추가 가능
		postMapper.deletePost(postId);
	}

	@Override
	public PostListResponse getPostList(Long boardId, int page, int size, String sort, String keyword) {
		int offset = page * size;
        String sortQuery = (sort != null && !sort.isBlank()) ? sort : "created_at desc";

        List<PostDetailResponse> posts = postMapper.findPostList(boardId, offset, size, sortQuery, keyword);
        long totalElements = postMapper.countPostList(boardId, keyword);
        int totalPages = (int) Math.ceil((double) totalElements / size);

        PostListResponse response = new PostListResponse();
        response.setContent(posts);
        response.setPage(page);
        response.setSize(size);
        response.setTotalElements((int) totalElements);
        response.setTotalPages(totalPages);

        return response;
	}

}
