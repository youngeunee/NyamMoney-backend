package com.ssafy.project.api.v1.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyTxStat {
    private int hour;
    private long amount;
    private boolean impulse;
}
