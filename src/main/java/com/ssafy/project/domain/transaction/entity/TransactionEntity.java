package com.ssafy.project.domain.transaction.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ssafy.project.domain.transaction.model.SalesType;
import com.ssafy.project.domain.transaction.model.TxType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity {
	private Long transactionId;
    private Long userId;

    private LocalDateTime occurredAt;
    private BigDecimal amount;
    private TxType txType;  // income / expense / transfer

    private Long categoryId;
    private String merchantNameRaw;

    private String paymentMethod;
    private String memo;
    private String tags;

    private String source; // 직접 등록, 농협 카드 ... 
    private String cardAuthNo;
    private SalesType salesType;
    private Integer installmentMonths;
    private String currencyCode;

    private boolean impulseFlag;
    private boolean isRefund;
    private LocalDateTime canceledAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
