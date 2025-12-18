package com.ssafy.project.api.v1.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.service.ChallengeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
	private final ChallengeService challengeService;
	public ChallengeController(ChallengeService challengeService) {
		this.challengeService = challengeService;
	}
	
	@GetMapping
	public ResponseEntity<ChallengeListResponse> getChallenge() {
		return ResponseEntity.ok(challengeService.getChallengeList());
	}
	
	@PostMapping
	public ResponseEntity<ChallengeCreateResponse> createChallenge(
			@RequestBody @Valid ChallengeCreateRequest request){
		ChallengeCreateResponse response = challengeService.createChallenge(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	

}
