package com.ssafy.project.api.v1.userFollow.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.userFollow.model.UserFollowStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowDto {
    private Long followId;
    private Long followerId;
    private Long followeeId;
    private UserFollowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}