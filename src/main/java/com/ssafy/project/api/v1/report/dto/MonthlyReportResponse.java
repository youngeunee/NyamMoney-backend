package com.ssafy.project.api.v1.report.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyReportResponse {
	private int year;
    private int month;

    private long totalSpend;
    private long impulseSpend;
    private double impulseRatio;

    private String topCategory;
    private List<CategoryStats> categoryStats;

    private String aiSummary;
    private String emotionSummary;

}
