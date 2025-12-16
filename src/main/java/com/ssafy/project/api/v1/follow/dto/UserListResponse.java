package com.ssafy.project.api.v1.follow.dto;

import java.util.List;

import com.ssafy.project.api.v1.user.dto.UserDetailResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserListResponse {

    private int totalCount;
    private List<UserDetailResponse> items;
}
