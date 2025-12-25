package com.ssafy.project.api.v1.transaction.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TransactionSummaryQuery {
    private Long userId;
    private LocalDateTime from;
    private LocalDateTime to;
    private String q;
}