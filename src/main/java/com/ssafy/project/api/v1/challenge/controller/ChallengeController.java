package com.ssafy.project.api.v1.challenge.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.service.ChallengeService;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
	private final ChallengeService challengeService;
	public ChallengeController(ChallengeService challengeService) {
		this.challengeService = challengeService;
	}
	
	@GetMapping
	public ResponseEntity<ChallengeListResponse> getChallenges() {
		return ResponseEntity.ok(challengeService.getChallengeList());
	}

}
