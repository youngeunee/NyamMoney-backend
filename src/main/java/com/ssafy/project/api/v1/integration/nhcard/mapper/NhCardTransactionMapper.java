package com.ssafy.project.api.v1.integration.nhcard.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;

@Mapper
public interface NhCardTransactionMapper {
    int upsertNhCardTransaction(TransactionUpsertParam p);
}
