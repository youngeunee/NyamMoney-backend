package com.ssafy.project.api.v1.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.ChallengeUpdateRequest;
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
	
	@PatchMapping("/{challengeId}")
	public ResponseEntity<Void> updateChallenge(
            @PathVariable Long challengeId,
            @RequestBody @Valid ChallengeUpdateRequest request
    ) {
        challengeService.updateChallenge(challengeId, request);
        return ResponseEntity.noContent().build();
    }
	
	@DeleteMapping("/{challengeId}")
	public ResponseEntity<Void> deleteChallenge(@PathVariable Long challengeId) {
        challengeService.deleteChallenge(challengeId);
        return ResponseEntity.noContent().build();
    }
	

}
