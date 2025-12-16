package com.ssafy.project.api.v1.follow.dto;

import com.ssafy.project.domain.follow.model.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@AllArgsConstructor
@Builder
public class FollowRequestApproveRequest {

    private Status status; // ACCEPTED, REJECTED
}
