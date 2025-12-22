package com.ssafy.project.api.v1.transaction.service;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionCursorRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionDailySummaryItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateRequest;
import com.ssafy.project.common.dto.CursorPage;

public interface TransactionService {

	TransactionCreateResponse createTransaction(Long userId, TransactionCreateRequest req);

	TransactionDetailResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest req);

	void deleteTransaction(Long userId, Long transactionId);

	TransactionDetailResponse getTransactionDetail(Long userId, Long transactionId);

	TransactionSummaryResponse getSummary(Long userId, LocalDateTime start, LocalDateTime end);
	
	public CursorPage<TransactionItem> getTransactions(Long userId, TransactionCursorRequest req);

	List<TransactionDailySummaryItem> getDailySummary(Long userId, LocalDateTime start, LocalDateTime end);
}
