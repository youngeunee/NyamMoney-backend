package com.ssafy.project.api.v1.transaction.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.service.TransactionService;
import com.ssafy.project.security.auth.UserPrincipal;

import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
	
	private final TransactionService transactionService;
	
	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	@PostMapping
	public ResponseEntity<TransactionCreateResponse> createTransaction(@AuthenticationPrincipal UserPrincipal principal, @RequestBody TransactionCreateRequest req) {
		Long userId = principal.getUserId();
		
		TransactionCreateResponse res = transactionService.createTransaction(userId, req);
		
		return ResponseEntity.ok(res);
	}
}
