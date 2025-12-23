package com.ssafy.project.api.v1.openai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiResponsesRequest {

    private String model;
    private List<InputMessageDto> input;
    private List<ToolDto> tools;
    private Double temperature;

}