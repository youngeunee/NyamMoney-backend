package com.ssafy.project.api.v1.openai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.openai.caller.OpenAiResponsesCaller;
import com.ssafy.project.api.v1.openai.dto.ContentItemDto;
import com.ssafy.project.api.v1.openai.dto.InputMessageDto;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesRequest;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesResponse;
import com.ssafy.project.api.v1.openai.dto.OutputItemDto;
import com.ssafy.project.api.v1.openai.dto.ToolDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantCategoryAiService {

    private final OpenAiResponsesCaller openAiResponsesCaller;
    private final ObjectMapper objectMapper;

    public String classifySingleDigitCode(String merchantName) {

        OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
                .model("gpt-5-nano")
                .input(List.of(
                        new InputMessageDto("developer", "한국어로 답하라."),
                        new InputMessageDto("developer",
                                "출력 규칙: 반드시 0~9 중 한 글자만 출력. 공백/설명/JSON 금지. 애매하면 0."),
                        new InputMessageDto("developer",
                                "카테고리 코드: 1=식사,2=카페·간식,3=쇼핑,4=이동·차량,5=주거·생활요금,6=건강·의료,7=교육,8=여가·취미,9=금융,0=기타"),
                        new InputMessageDto("user",
                                "상호명: " + merchantName + ". 웹검색 후 카테고리 코드 한 글자만 출력.")
                ))
                .tools(List.of(
                        ToolDto.builder().type("web_search").build()
                ))
                .build();
        try {
			log.info("[OPENAI][REQ] {}", objectMapper.writeValueAsString(req));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);
        log.info("[OPENAI][RAW RESPONSE] {}", resp);

        String raw = extractOutputText(resp);
        String code = raw == null ? "" : raw.trim();

        // ✅ 마지막 방어: 무조건 한 글자
        if (!code.matches("^[0-9]$")) return "0";
        return code;
    }

    private String extractOutputText(OpenAiResponsesResponse resp) {
        if (resp == null || resp.getOutput() == null) return null;

        for (OutputItemDto out : resp.getOutput()) {
            if (!"message".equals(out.getType())) continue;
            if (out.getContent() == null) continue;

            for (ContentItemDto c : out.getContent()) {
                if ("output_text".equals(c.getType()) && c.getText() != null && !c.getText().isBlank()) {
                    return c.getText();
                }
            }
        }
        return null;
    }
}