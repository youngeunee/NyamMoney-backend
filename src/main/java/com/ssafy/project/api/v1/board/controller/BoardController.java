package com.ssafy.project.api.v1.board.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.board.dto.BoardListResponse;
import com.ssafy.project.api.v1.board.service.BoardService;

@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
	private final BoardService boardService;
	public BoardController(BoardService boardService) {
		this.boardService = boardService;
	}
	
	@GetMapping
	public ResponseEntity<BoardListResponse> getBoards(){
		BoardListResponse response = boardService.getBoardList();
		return ResponseEntity.ok(response);
	}
}
