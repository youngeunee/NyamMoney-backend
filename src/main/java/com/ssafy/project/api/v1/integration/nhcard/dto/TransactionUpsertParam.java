package com.ssafy.project.api.v1.integration.nhcard.dto;

import java.time.LocalDateTime;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionUpsertParam {
    private Long userId;
    private LocalDateTime occurredAt;
    private long amount;
    private String txType;

    private Long categoryId;
    private String categoryMethod;

    private String merchantNameRaw;
    private String paymentMethod;
    private String memo;
    private String tags;

    private String source;
    private String cardAuthNo;
    private String salesType;
    private Integer installmentMonths;
    private String currencyCode;

    private int impulseFlag;
    private int isRefund;
    private LocalDateTime canceledAt;
}
