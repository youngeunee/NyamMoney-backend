package com.ssafy.project.api.v1.follow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
@Builder
@NoArgsConstructor
public class FollowRequestCancelResponse {
    private Long requestId;
    private String status; // "CANCELED"
}
