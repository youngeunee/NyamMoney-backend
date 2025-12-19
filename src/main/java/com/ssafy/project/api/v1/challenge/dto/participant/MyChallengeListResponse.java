package com.ssafy.project.api.v1.challenge.dto.participant;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyChallengeListResponse {
	private List<MyChallengeItem> items;
}
