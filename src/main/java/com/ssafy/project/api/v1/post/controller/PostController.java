package com.ssafy.project.api.v1.post.controller;

import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.post.dto.PostCreateRequest;
import com.ssafy.project.api.v1.post.dto.PostCreateResponse;
import com.ssafy.project.api.v1.post.dto.PostDetailResponse;
import com.ssafy.project.api.v1.post.dto.PostListResponse;
import com.ssafy.project.api.v1.post.dto.PostUpdateRequest;
import com.ssafy.project.api.v1.post.service.PostService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/posts")
public class PostController {
	private final PostService postService;
	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	@GetMapping("/{postId}")
	public PostDetailResponse getPostDetail(@PathVariable Long postId, @PathVariable Long boardId,
			@AuthenticationPrincipal(expression = "userId") Long userId) throws NotFoundException {
		return postService.getPostDetail(boardId, postId, userId);
		
	}

	@PostMapping
	public PostCreateResponse createPost(@PathVariable Long boardId, 
			@Valid @RequestBody PostCreateRequest dto,
			@AuthenticationPrincipal(expression = "userId") Long userId) {
		return postService.createPost(boardId, userId, dto);
	}
	
	@PatchMapping("/{postId}")
	public ResponseEntity<PostDetailResponse> updatePost(@PathVariable Long boardId, @PathVariable Long postId,
			@AuthenticationPrincipal(expression = "userId") Long userId,
			@RequestBody @Valid PostUpdateRequest req) {
		PostDetailResponse updated = postService.updatePost(postId, userId, req);
		return ResponseEntity.ok(updated);
	}
	
	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(@PathVariable Long boardId, @PathVariable Long postId,
			@AuthenticationPrincipal(expression = "userId") Long userId){ 
		postService.deletePost(boardId, postId, userId);
		return ResponseEntity.noContent().build(); // 204 반환
	}
	
	@GetMapping
	public ResponseEntity<PostListResponse> getPosts(@PathVariable Long boardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword){
		PostListResponse response = postService.getPostList(boardId, page, size, sort, keyword);
        return ResponseEntity.ok(response);
	}

}
