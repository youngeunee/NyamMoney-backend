package com.ssafy.project.domain.userFollwer.entity;

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
public class UserFollowEntity {
    private Long followId;
    private Long followerId;
    private Long followeeId;
    private UserFollowStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}