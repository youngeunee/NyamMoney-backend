package com.ssafy.project.api.v1.follow.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.follow.model.Status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowRequestApproveResponse {

    private Long requestId;      // followId
    private Long followerId;     // 요청 보낸 사용자
    private Long followeeId;     // 요청 받은 사용자(=나)
    private Status status;       // ACCEPTED / REJECTED
    private LocalDateTime updatedAt;
}
