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
		 // 1) categoryId 존재 확인
        CategoryItem category = categoryMapper.selectCategoryItemById(req.getCategoryId());
        if (category == null) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        // 2) insert
        // pk return하기 위해 변환하기
        TransactionCreateParam p = TransactionCreateParam.from(userId, req);

        Long txId = transactionMapper.insertAndReturnId(p);
        if (txId == null) {
            throw new IllegalStateException("거래내역 생성에 실패했습니다. (transactionId 누락)");
        }

        // 3) 응답
        TransactionCreateResponse res = transactionMapper.selectCreateResponseById(userId, txId);
        
        if (res == null) {
            throw new IllegalStateException("거래내역 생성 후 조회에 실패했습니다.");
        }
        
        // categoryItem 재사용
        res.setCategory(category);
        return res;
	}

}
