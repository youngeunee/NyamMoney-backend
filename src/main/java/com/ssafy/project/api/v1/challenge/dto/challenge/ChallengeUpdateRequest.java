package com.ssafy.project.api.v1.challenge.dto.challenge;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChallengeUpdateRequest {
	@NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotNull
    private LocalDateTime startDate;
    @NotNull
    private LocalDateTime endDate;

}
