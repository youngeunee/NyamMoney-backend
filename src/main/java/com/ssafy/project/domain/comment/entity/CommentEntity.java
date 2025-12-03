package com.ssafy.project.domain.comment.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String contentMd;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}
