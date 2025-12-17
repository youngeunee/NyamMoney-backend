package com.ssafy.project.api.v1.post.service;

import java.util.List;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.comment.mapper.CommentMapper;
import com.ssafy.project.api.v1.post.dto.Post;
import com.ssafy.project.api.v1.post.dto.PostCreateRequest;
import com.ssafy.project.api.v1.post.dto.PostCreateResponse;
import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostDto;
import com.ssafy.project.api.v1.post.dto.PostListResponse;
import com.ssafy.project.api.v1.post.dto.PostUpdateRequest;
import com.ssafy.project.api.v1.post.mapper.PostMapper;
import com.ssafy.project.api.v1.postLike.mapper.PostLikeMapper;

import jakarta.validation.Valid;

@Service
public class PostServiceImpl implements PostService {
	private final PostMapper postMapper;
	private final PostLikeMapper postLikeMapper;
	private final CommentMapper commentMapper;
	public PostServiceImpl(PostMapper postMapper, PostLikeMapper postLikeMapper, CommentMapper commentMapper) {
		this.postMapper = postMapper;
		this.postLikeMapper = postLikeMapper;
		this.commentMapper = commentMapper;
	}

	@Override
	public PostDetailResponse getPostDetail(Long boardId, Long postId, Long userId) throws NotFoundException {
		// 1. 게시글 존재 확인
	    if (postMapper.existsPost(boardId, postId) == 0) {
	        throw new NotFoundException("게시글 없음");
	    }

	    // 2. 게시글 상세 조회 (기존 메서드 그대로 사용)
	    PostDetailResponse response = postMapper.getPostDetail(postId);

	    // 3. 좋아요 여부 계산
	    boolean liked = postLikeMapper.existsUserLike(postId, userId) > 0;
	    response.setLiked(liked);

	    return response;
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

	@Transactional
	@Override
	public void deletePost(Long boardId, Long postId, Long userId) throws NotFoundException {
		// 게시글 존재 + 권한 검증
	    Post post = postMapper.findById(postId);
	    if (post == null || post.getDeletedAt() != null) {
	        throw new NotFoundException("게시글 없음");
	    }
	    // 댓글 소프트 삭제
	    commentMapper.softDeleteByPostId(postId);
	    // 게시글 소프트 삭제
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
