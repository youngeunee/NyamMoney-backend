package com.ssafy.project.api.v1.postLike.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostLikeDto {
    private Long likedId;
    private Long postId;
    private Long userId;
    private LocalDateTime createdAt;
}