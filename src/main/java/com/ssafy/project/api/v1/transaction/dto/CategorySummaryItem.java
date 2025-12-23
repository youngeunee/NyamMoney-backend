package com.ssafy.project.api.v1.transaction.dto;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummaryItem {
	private Long categoryId;
    private String categoryName;
    private Long amount;
}
