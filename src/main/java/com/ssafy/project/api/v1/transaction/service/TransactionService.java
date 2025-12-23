package com.ssafy.project.api.v1.transaction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalItem;
import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;
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

	int syncNhTransactions(Long userId, LocalDate fromDate, LocalDate toDate);

	/**
	 * ✅ 테스트/운영 공용: "적재(insert) + 분류(RULE/VECTOR/OPENAI)"만 수행
	 * - 더미 NhCardApprovalItem 리스트를 만들어 이 메서드만 호출하면 검증 끝
	 */
	int ingestNhApprovalItems(Long userId, List<NhCardApprovalItem> items);
		
	Long mapCodeToCategoryId(String code);

	TransactionUpsertParam mapToUpsertParam(NhCardApprovalItem it, Long userId);
}
