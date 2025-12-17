package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;

import com.ssafy.project.domain.transaction.model.TxType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreateParam {
    private Long transactionId; // DB가 채움

    private Long userId;

    private TxType txType;
    private Long amount;
    private LocalDateTime occurredAt;
    private Long categoryId;
    private String merchantName;
    private String memo;
    private Boolean isRefund;
    private LocalDateTime canceledAt;
    
    public static TransactionCreateParam from(
            Long userId,
            TransactionCreateRequest req
    ) {
        TransactionCreateParam p = new TransactionCreateParam();
        p.userId = userId;
        p.txType = req.getTxType();
        p.amount = req.getAmount();
        p.occurredAt = req.getOccurredAt();
        p.categoryId = req.getCategoryId();
        p.merchantName = req.getMerchantName();
        p.memo = req.getMemo();
        p.isRefund = req.getIsRefund();
        p.canceledAt = req.getCanceledAt();
        return p;
    }
}
