package com.ssafy.project.api.v1.openai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ToolDto {
    private String type; // web_search
    private String search_context_size;
    private UserLocationDto user_location;
}
