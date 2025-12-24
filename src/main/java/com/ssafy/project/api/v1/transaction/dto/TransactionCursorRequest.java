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
public class TransactionCursorRequest {
    private LocalDateTime from;
    private LocalDateTime to;
    
    private String q; 
    
    private String cursor;
    private Integer size;
}