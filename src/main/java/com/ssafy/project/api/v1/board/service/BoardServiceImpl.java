package com.ssafy.project.api.v1.board.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.board.dto.BoardListItem;
import com.ssafy.project.api.v1.board.dto.BoardListResponse;
import com.ssafy.project.api.v1.board.mapper.BoardMapper;

@Service
public class BoardServiceImpl implements BoardService {
	private final BoardMapper boardMapper;
	public BoardServiceImpl(BoardMapper boardMapper) {
		this.boardMapper = boardMapper;
	}

	@Override
	public BoardListResponse getBoardList() {
		List<BoardListItem> list = boardMapper.getBoardList();
		
		BoardListResponse res = new BoardListResponse();
		res.setItems(list);
		return res;
	}

}
