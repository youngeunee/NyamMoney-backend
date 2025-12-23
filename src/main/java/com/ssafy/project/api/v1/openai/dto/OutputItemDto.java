package com.ssafy.project.api.v1.openai.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OutputItemDto {

    private String type; // message, reasoning, web_search_call
    private List<ContentItemDto> content;
}
