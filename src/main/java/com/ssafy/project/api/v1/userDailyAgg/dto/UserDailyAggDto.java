package com.ssafy.project.api.v1.userDailyAgg.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDailyAggDto {
    private Long reportId;
    private Long userId;
    private LocalDate day;
    private BigDecimal totalSpend;
    private BigDecimal impulseSpend;
    private BigDecimal dessertSpend;
    private BigDecimal foodSpend;
    private BigDecimal shoppingSpend;
    private BigDecimal etcSpend;
}
