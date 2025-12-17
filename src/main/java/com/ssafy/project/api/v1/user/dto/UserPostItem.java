package com.ssafy.project.api.v1.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
@NoArgsConstructor
public class UserPostItem {
	private Long postId;

    private Long boardId;
    private String boardName;

    private String title;
    private String excerpt;      // 한두 줄 미리보기
    private String rawContent; // 원문
    
    private LocalDateTime createdAt;

    private Integer likeCount;
    private Integer commentCount;
}
