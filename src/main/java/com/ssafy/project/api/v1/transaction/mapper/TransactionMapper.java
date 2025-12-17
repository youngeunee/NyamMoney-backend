package com.ssafy.project.api.v1.transaction.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;

@Mapper
public interface TransactionMapper {
	// insert 후 p.transactionId가 채워짐 (useGeneratedKeys)
    int insertTransaction(TransactionCreateParam p);

    // 생성 직후 응답 조회 (resultMap + category join)
    TransactionCreateResponse selectCreateResponseById(@Param("userId") Long userId,
                                                       @Param("transactionId") Long transactionId);
	
	
}
