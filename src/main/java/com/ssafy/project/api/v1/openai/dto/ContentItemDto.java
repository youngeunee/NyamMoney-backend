package com.ssafy.project.api.v1.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class ContentItemDto {
    private String type; // output_text
    private String text;
    
    @Override
	public String toString() {
    	return text;
    }
}
