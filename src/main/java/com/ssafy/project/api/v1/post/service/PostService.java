package com.ssafy.project.api.v1.post.service;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.post.dto.PostDto;
import com.ssafy.project.api.v1.post.mapper.PostMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostMapper postMapper;

	public int create(PostDto dto) {
		return postMapper.create(dto);
		
	}

}
