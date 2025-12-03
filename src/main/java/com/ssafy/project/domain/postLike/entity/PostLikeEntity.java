package com.ssafy.project.domain.postLike.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeEntity {
    private Long likedId;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}