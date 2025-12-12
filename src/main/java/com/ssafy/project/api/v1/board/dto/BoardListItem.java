package com.ssafy.project.api.v1.board.dto;

import lombok.Data;

@Data
public class BoardListItem {
	
	private Long boardId;
	private String name;
	private int postCount;

}
