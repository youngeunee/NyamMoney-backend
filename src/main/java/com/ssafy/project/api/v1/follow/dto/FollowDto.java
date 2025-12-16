package com.ssafy.project.api.v1.follow.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.follow.model.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowDto {
    private Long followId;
    private Long followerId;
    private Long followeeId;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
