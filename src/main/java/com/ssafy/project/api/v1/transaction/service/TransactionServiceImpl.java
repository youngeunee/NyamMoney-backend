package com.ssafy.project.api.v1.transaction.service;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionDto;
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

        // 1) categoryId 존재 검증 (선택: FK가 있으면 DB가 막지만, 메시지/검증을 위해 둠)
        boolean categoryExists = categoryMapper.existsById(req.getCategoryId());
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

}
