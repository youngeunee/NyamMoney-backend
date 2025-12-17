package com.ssafy.project.api.v1.transaction.service;

import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;

public interface TransactionService {

	TransactionCreateResponse createTransaction(Long userId, TransactionCreateRequest req);
	
}
