package com.ssafy.project.api.v1.board.dto;

import java.util.List;

import lombok.Data;

@Data
public class BoardListResponse {
	private List<BoardListItem> items;

}
