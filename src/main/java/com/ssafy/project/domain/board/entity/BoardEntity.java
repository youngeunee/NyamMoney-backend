package com.ssafy.project.domain.board.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardEntity {
    private Long boardId;
    private String slug;
    private String name;
}
