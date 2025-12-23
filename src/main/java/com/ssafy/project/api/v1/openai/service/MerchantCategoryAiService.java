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
import com.ssafy.project.api.v1.openai.dto.UserLocationDto;

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

    		        // 1️ 역할 + 절차 강제 (웹 검색 선행)
    		        new InputMessageDto(
    		            "developer",
    		            "당신은 가맹점 상호명을 카테고리 코드(0~9)로 분류하는 분류기입니다. "
    		          + "반드시 web_search를 먼저 사용하여 상호명을 검색하세요. "
    		          + "웹 검색 결과에서 업종, 제공 서비스, 매장 성격을 먼저 파악한 뒤 "
    		          + "그 업종 정보를 근거로 카테고리 코드를 해석해야 합니다. "
    		          + "웹 검색 없이 추측으로 분류하는 것은 금지됩니다."
    		        ),

    		        // 2️ 업종 → 카테고리 매핑 규칙
    		        new InputMessageDto(
    		            "developer",
    		            "카테고리 코드 정의: "
    		          + "1=식사(식당, 한식/중식/일식/양식, 패스트푸드, 배달음식, 치킨/피자, 주점 포함), "
    		          + "2=카페·간식(카페, 커피, 베이커리, 디저트, 아이스크림, 간식류), "
    		          + "3=쇼핑(편의점, 마트, 백화점, 온라인몰, 의류/잡화/가전/생활용품), "
    		          + "4=이동·차량(주유소, 전기차 충전, 차량정비, 주차, 렌터카, 대중교통), "
    		          + "5=주거·생활요금(통신, 전기/가스/수도, 관리비, 생활서비스), "
    		          + "6=건강·의료(병원, 의원, 치과, 약국, 검진), "
    		          + "7=교육(학원, 교육기관, 강의, 교재), "
    		          + "8=여가·취미(영화, 공연, 여행, 숙박, 스포츠, 취미활동), "
    		          + "9=금융(은행, 카드사, 보험, 증권, 이자, 수수료), "
    		          + "0=기타(검색 결과 부족, 업종 불명확, 판단 불가)"
    		        ),

    		        // 3️ 우선순위 + 실패 조건
    		        new InputMessageDto(
    		            "developer",
    		            "업종이 여러 개로 해석될 경우 다음 우선순위를 따르세요: "
    		          + "이동·차량 > 건강·의료 > 교육 > 쇼핑 > 카페·간식 > 식사 > 여가·취미 > 금융 > 기타. "
    		          + "다음 경우에는 반드시 0을 출력하세요: "
    		          + "웹 검색 결과가 상호명과 명확히 일치하지 않음, "
    		          + "업종을 특정할 수 있는 정보가 부족함, "
    		          + "상호명이 지점명/약칭뿐이라 업종 단서가 없음."
    		        ),

    		        // 4️ 출력 형식 강제
    		        new InputMessageDto(
    		            "developer",
    		            "출력 규칙: 최종 출력은 반드시 0~9 중 한 글자만 출력. "
    		          + "공백, 줄바꿈, 설명, JSON, 따옴표 모두 금지."
    		        ),

    		        // 5️ User 입력 (검색 방식까지 명시)
    		        new InputMessageDto(
    		            "user",
    		            "상호명: " + merchantName + ". "
    		          + "반드시 web_search로 상호명을 먼저 검색하세요. "
    		          + "검색 시 '상호명 + 업종', '상호명 + 매장', '상호명 + 메뉴' 형태도 함께 시도하세요. "
    		          + "웹 검색으로 확인한 업종을 근거로 카테고리 코드 한 글자만 출력하세요."
    		        )
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
        logWebSearch(resp);

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

    private void logWebSearch(OpenAiResponsesResponse resp) {
        if (resp == null || resp.getOutput() == null) return;

        for (OutputItemDto out : resp.getOutput()) {
            if (out == null || out.getType() == null) continue;

            if ("web_search_call".equals(out.getType())) {
                log.info("[OPENAI][WEB SEARCH CALL] {}", out);
            } else if ("web_search_result".equals(out.getType())) {
                log.info("[OPENAI][WEB SEARCH RESULT] {}", out);
            }
        }
    }
}
