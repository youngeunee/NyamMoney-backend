package com.ssafy.project.api.v1.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryStats {
	private Long categoryId;
    private String categoryName;
    private long totalSpend;
    private long impulseSpend;
    private double impulseRatio;
}
