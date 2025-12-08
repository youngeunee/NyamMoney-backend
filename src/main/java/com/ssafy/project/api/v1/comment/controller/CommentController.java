package com.ssafy.project.api.v1.comment.controller;

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

import com.ssafy.project.api.v1.comment.dto.CommentCreateRequest;
import com.ssafy.project.api.v1.comment.dto.CommentCreateResponse;
import com.ssafy.project.api.v1.comment.dto.CommentDetailResponse;
import com.ssafy.project.api.v1.comment.dto.CommentUpdateRequest;
import com.ssafy.project.api.v1.comment.service.CommentService;

@RestController
@RequestMapping("/api/boards/{boardId}/posts/{postId}/comments")
public class CommentController {
	private final CommentService commentService;
	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}
	
	@GetMapping("/{commentId}")
	public CommentDetailResponse getComment(@PathVariable Long boardId,
											@PathVariable Long postId,
											@PathVariable Long commentId) {
		return commentService.getComment(boardId, postId, commentId);
	}
	
	@PostMapping
	public CommentCreateResponse createComment(@PathVariable Long postId, @RequestParam Long userId,
											   @RequestBody CommentCreateRequest req) {
		
		return commentService.createComment(postId, userId, req);
	}
	
	@PatchMapping("/{commentId}")
	public CommentDetailResponse updateComment(@PathVariable Long postId, @PathVariable Long commentId, @RequestBody CommentUpdateRequest req,
												@RequestParam Long userId) {
		return commentService.updateComment(postId, commentId, req, userId);
	}
	
//	@DeleteMapping("/{commentId}")
//	public ResponseEntity<Void> deleteComment(@PathVariable Long boardId, @PathVariable Long postId,
//												@PathVariable Long commentId, @AuthenticationPrincipal String userIdStr) throws NotFoundException, Exception{
//		Long userId = Long.parseLong(userIdStr);
//		commentService.deleteComment(boardId, postId, commentId, userId);
//		return ResponseEntity.noContent().build();
//	}
	
	@DeleteMapping("/{commentId}")
	public ResponseEntity<Void> deleteComment(
	        @PathVariable Long boardId,
	        @PathVariable Long postId,
	        @PathVariable Long commentId,
	        @AuthenticationPrincipal String userIdStr) throws Exception {

	    if (userIdStr == null || userIdStr.equals("anonymousUser")) {
	        return ResponseEntity.status(401).build(); // Unauthorized
	    }

	    Long userId = Long.parseLong(userIdStr);
	    commentService.deleteComment(boardId, postId, commentId, userId);
	    return ResponseEntity.noContent().build();
	}

}
