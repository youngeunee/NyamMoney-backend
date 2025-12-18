package com.ssafy.project.api.v1.challenge.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChallengeCreateRequest {
	@NotBlank(message = "제목은 필수입니다.")
    private String title;
	@NotBlank(message = "설명은 필수입니다.")
    private String description;
	@NotNull(message = "예산 한도는 필수입니다.")
    @PositiveOrZero(message = "예산 한도는 0 이상이어야 합니다.")
	private Long budgetLimit;
	@NotNull(message = "시작일은 필수입니다.")
    private LocalDateTime startDate;
	@NotNull(message = "종료일은 필수입니다.")
    private LocalDateTime endDate;

}
