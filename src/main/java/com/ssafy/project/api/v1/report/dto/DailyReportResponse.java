package com.ssafy.project.api.v1.report.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyReportResponse {
    private String date;
    private long totalSpend;
    private long impulseSpend;
    private double impulseRatio;
    private List<CategoryStats> categoryStats;

    private String spendingRhythm;   // 소비 리듬 분석
    private String spendingDensity;  // 소비 밀도 분석
    private String dailyComment;     // 하루 마무리 코멘트
}