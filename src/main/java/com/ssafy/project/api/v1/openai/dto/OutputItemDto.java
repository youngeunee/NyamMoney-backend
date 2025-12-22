package com.ssafy.project.api.v1.openai.dto;

import java.util.List;

public class OutputItemDto {

    private String type; // message, reasoning, web_search_call
    private List<ContentItemDto> content;

    public OutputItemDto() {}

    public String getType() {
        return type;
    }

    public List<ContentItemDto> getContent() {
        return content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setContent(List<ContentItemDto> content) {
        this.content = content;
    }
}