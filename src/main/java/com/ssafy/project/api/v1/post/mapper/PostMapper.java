package com.ssafy.project.api.v1.post.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.post.dto.PostDto;

@Mapper
public interface PostMapper {

	int create(PostDto dto);

}
