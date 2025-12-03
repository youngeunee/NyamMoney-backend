package com.ssafy.project.api.v1.post.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.post.dto.PostDto;
import com.ssafy.project.api.v1.post.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {
	private final PostService postService;
	
	public ResponseEntity<String> create(@RequestBody PostDto dto){
		postService.create(dto);
		return ResponseEntity.ok("created");
	}

}
