package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ssafy.project.domain.transaction.model.TxType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCreateRequest {
	@NotNull
    private TxType txType; // INCOME, EXPENSE, TRANSFER

    @NotNull
    private Long amount;

    @NotNull
    private LocalDateTime occurredAt;

    @NotNull
    private Long categoryId;

    private String merchantName;

    private String memo;

    private Boolean isRefund;
    private LocalDateTime canceledAt;

    private List<String> tags;         // 분류/클러스터링 대비
}
