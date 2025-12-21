package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionItem {
    private Long transactionId;
    private LocalDateTime occurredAt;

    private Long amount;
    private String paymentMethod;
    private String merchantNameRaw;

    private Boolean impulseFlag;
    private Boolean isRefund;
}