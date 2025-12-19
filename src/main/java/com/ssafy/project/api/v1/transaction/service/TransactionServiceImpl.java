package com.ssafy.project.api.v1.transaction.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionDto;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryQuery;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateRequest;
import com.ssafy.project.api.v1.transaction.mapper.TransactionMapper;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    
    public TransactionServiceImpl(TransactionMapper transactionMapper, CategoryMapper categoryMapper) {
    	this.transactionMapper = transactionMapper;
    	this.categoryMapper = categoryMapper;
    }
	
    @Override
    public TransactionCreateResponse createTransaction(Long userId, TransactionCreateRequest req) {
    	if (req.getImpulseFlag() == null) {
    		req.setImpulseFlag(Boolean.FALSE);
    	}

        // 1) categoryId 존재 검증 (선택: FK가 있으면 DB가 막지만, 메시지/검증을 위해 둠)
        boolean categoryExists = categoryMapper.existsById(req.getCategoryId());
        System.out.println("DEBUG categoryId = " + req.getCategoryId());
        if (!categoryExists) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        // 2) Request -> Param (Insert 전용)
        TransactionCreateParam p = TransactionCreateParam.from(userId, req);

        // 3) insert (generated key가 p.transactionId에 채워짐)
        int affected = transactionMapper.insertTransaction(p);
        if (affected != 1 || p.getTransactionId() == null) {
            throw new IllegalStateException("거래내역 생성에 실패했습니다.");
        }

        // 4) 방금 생성한 row 조회 (resultMap으로 categoryItem까지 포함)
        TransactionCreateResponse res =
                transactionMapper.selectCreateResponseById(userId, p.getTransactionId());

        if (res == null) {
            throw new IllegalStateException("거래내역 생성 후 조회에 실패했습니다.");
        }

        return res;
    }

    @Override
    public TransactionDetailResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest req) {

        if (req.getCategoryId() != null) {
            boolean exists = categoryMapper.existsById(req.getCategoryId());
            if (!exists) {
                throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
            }
        }

        // 환불 체크하면 그냥 지금 시간으로 넣기
        if (Boolean.TRUE.equals(req.getIsRefund()) && req.getCanceledAt() == null) {
            req.setCanceledAt(LocalDateTime.now());
        }
        
        if (Boolean.FALSE.equals(req.getIsRefund())) {
            req.setCanceledAt(null);
        }

        // 업데이트하기 
        TransactionUpdateParam p = TransactionUpdateParam.from(userId, transactionId, req);
        int updated = transactionMapper.updateTransaction(p);

        if (updated != 1) {
            throw new IllegalArgumentException("거래내역을 찾을 수 없습니다.");
        }

        // 다시 조회해서 Return
        TransactionDetailResponse res = transactionMapper.selectDetailById(userId, transactionId);
        if (res == null) {
            throw new IllegalStateException("수정 후 조회에 실패했습니다.");
        }
        return res;
    }
    
    @Override
    public void deleteTransaction(Long userId, Long transactionId) {

        int updated = transactionMapper.deleteTransaction(userId, transactionId);

        if (updated != 1) {
            throw new IllegalArgumentException("삭제할 거래내역을 찾을 수 없습니다.");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionDetail(Long userId, Long transactionId) {
        TransactionDetailResponse res = transactionMapper.selectDetailById(userId, transactionId);
        if (res == null) {
            throw new IllegalArgumentException("거래내역을 찾을 수 없습니다.");
        }
        return res;
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummary(Long userId, LocalDateTime from, LocalDateTime to) {

        TransactionSummaryQuery q = new TransactionSummaryQuery(userId, from, to);

        TransactionSummaryResponse res = transactionMapper.selectSummary(q);

        if (res == null) {
            return TransactionSummaryResponse.builder()
                    .from(from)
                    .to(to)
                    .totalExpense(0L)
                    .totalImpulseExpense(0L)
                    .build();
        }

        return TransactionSummaryResponse.builder()
                .from(from)
                .to(to)
                .totalExpense(res.getTotalExpense() == null ? 0L : res.getTotalExpense())
                .totalImpulseExpense(res.getTotalImpulseExpense() == null ? 0L : res.getTotalImpulseExpense())
                .build();
    }

}
