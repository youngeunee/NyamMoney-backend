package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;

import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.domain.transaction.model.TxType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class TransactionCreateResponse {
	private Long transactionId;

    private TxType txType;

    private Long amount;

    private LocalDateTime occurredAt;

    private CategoryItem category;

    private String merchantName;

    private String memo;

    private Boolean isRefund;

    private LocalDateTime canceledAt;

    private LocalDateTime createdAt;
}
