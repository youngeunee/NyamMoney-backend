package com.ssafy.project.api.v1.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.board.dto.BoardListItem;

@Mapper
public interface BoardMapper {

	List<BoardListItem> getBoardList();

}
