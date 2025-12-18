package com.ssafy.project.api.v1.challenge.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChallengeCreateRequest {
	@NotBlank
    private String title;
	@NotBlank
    private String description;
    @NotBlank
    private LocalDateTime startDate;
    @NotBlank
    private LocalDateTime endDate;

}
