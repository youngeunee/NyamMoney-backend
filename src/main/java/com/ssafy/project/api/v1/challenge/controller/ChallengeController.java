package com.ssafy.project.api.v1.challenge.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateRequest;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeCreateResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeDetailResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeListResponse;
import com.ssafy.project.api.v1.challenge.dto.challenge.ChallengeUpdateRequest;
import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeJoinResponse;
import com.ssafy.project.api.v1.challenge.dto.participant.ChallengeParticipantListResponse;
import com.ssafy.project.api.v1.challenge.dto.participant.MyChallengeListResponse;
import com.ssafy.project.api.v1.challenge.service.ChallengeParticipantService;
import com.ssafy.project.api.v1.challenge.service.ChallengeService;
import com.ssafy.project.security.auth.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
	private final ChallengeService challengeService;
	private final ChallengeParticipantService pService;
	public ChallengeController(ChallengeService challengeService, ChallengeParticipantService pService) {
		this.challengeService = challengeService;
		this.pService = pService;
	}
	
	@GetMapping
	public ResponseEntity<ChallengeListResponse> getChallenge(@AuthenticationPrincipal UserPrincipal user) {
		Long userId = (user != null) ? user.getUserId() : null;
		return ResponseEntity.ok(challengeService.getChallengeList(userId));
	}
	
	// 단일 챌린지 상세조회
	@GetMapping("/{challengeId}")
	public ResponseEntity<ChallengeDetailResponse> getChallengeDetail(@PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal user) {
		Long userId = (user != null) ? user.getUserId() : null;
		ChallengeDetailResponse response = challengeService.getChallengeDetail(challengeId, userId);
		return ResponseEntity.ok(response);
	}
	
	@PostMapping
	public ResponseEntity<ChallengeCreateResponse> createChallenge(
			@RequestBody @Valid ChallengeCreateRequest request, @AuthenticationPrincipal UserPrincipal user){
		Long userId = (user != null) ? user.getUserId() : null;
		ChallengeCreateResponse response = challengeService.createChallenge(request, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@PatchMapping("/{challengeId}")
	public ResponseEntity<Void> updateChallenge(
            @PathVariable Long challengeId,
            @RequestBody @Valid ChallengeUpdateRequest request, @AuthenticationPrincipal UserPrincipal user) throws Exception {
		Long userId = (user != null) ? user.getUserId() : null;
        challengeService.updateChallenge(challengeId, request, userId);
        return ResponseEntity.noContent().build();
    }
	
	@DeleteMapping("/{challengeId}")
	public ResponseEntity<Void> deleteChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal UserPrincipal user) throws Exception {
		Long userId = (user != null) ? user.getUserId() : null;
        challengeService.deleteChallenge(challengeId, userId);
        return ResponseEntity.noContent().build();
    }
	
	// 참여한 챌린지 조회
	@GetMapping("/me")
	public ResponseEntity<MyChallengeListResponse> getMyChallenges(
            @AuthenticationPrincipal UserPrincipal user) {
		Long userId = (user != null) ? user.getUserId() : null;
        return ResponseEntity.ok(pService.getMyChallenges(userId));
    }
	
	@PostMapping("/{challengeId}/join")
	public ResponseEntity<ChallengeJoinResponse> joinChallenge(@PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal user) {
        ChallengeJoinResponse response = pService.joinChallenge(challengeId, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
	
	@DeleteMapping("/{challengeId}/join")
	public ResponseEntity<Void> cancelChallengeJoin(@PathVariable Long challengeId,
            @AuthenticationPrincipal UserPrincipal user) {
		Long userId = (user != null) ? user.getUserId() : null;
		pService.cancelChallengeJoin(challengeId, userId);
		return ResponseEntity.noContent().build();  // 204 No Content
	}
	
	// 특정 챌린지 참여 중인 사용자 목록 조회
	@GetMapping("/{challengeId}/participants")
	public ResponseEntity<ChallengeParticipantListResponse> getChallengeParticipants(@PathVariable Long challengeId) {
	    return ResponseEntity.ok(pService.getChallengeParticipants(challengeId));
	}

}
