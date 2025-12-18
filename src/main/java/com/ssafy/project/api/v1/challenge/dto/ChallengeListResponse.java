package com.ssafy.project.api.v1.challenge.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeListResponse {
	private List<ChallengeListItem> items;

}
