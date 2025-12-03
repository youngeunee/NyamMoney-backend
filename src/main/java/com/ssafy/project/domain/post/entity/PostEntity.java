package com.ssafy.project.domain.post.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostEntity {
    private Long postId;
    private Long boardId;
    private Long userId;
    private Long challengeId;
    private String title;
    private String contentMd;
    private Integer likesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
