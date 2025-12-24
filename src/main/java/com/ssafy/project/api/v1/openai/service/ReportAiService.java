package com.ssafy.project.api.v1.openai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.openai.caller.OpenAiResponsesCaller;
import com.ssafy.project.api.v1.openai.dto.ContentItemDto;
import com.ssafy.project.api.v1.openai.dto.InputMessageDto;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesRequest;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesResponse;
import com.ssafy.project.api.v1.openai.dto.OutputItemDto;
import com.ssafy.project.api.v1.report.dto.PersonaResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportAiService {
	private final ObjectMapper objectMapper;
	private final OpenAiResponsesCaller openAiResponsesCaller;
	
	public String summarizeMonthly(
            int year,
            int month,
            long totalSpend,
            long impulseSpend,
            double impulseRatio,
            String topCategory) {
    	OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
    		    .model("gpt-5-nano")
    		    .input(List.of(

    		        // 역할 + 절차 강제 (웹 검색 선행)
    		        new InputMessageDto(
    		        		"developer",
    		        		"당신은 개인 소비 분석 결과를 사용자에게 설명하는 요약 AI입니다. "
    		        		+ "이미 계산된 수치를 바탕으로 설명만 해야 하며, "
    		        		+ "새로운 계산이나 추론은 절대 하면 안 됩니다. "
    		        		+ "반드시 JSON 형식으로만 응답해야 합니다."
    		        ),
    		        new InputMessageDto(
    	                    "developer",
    	                    "출력 규칙: JSON 객체 하나만 출력하며, "
    	                  + "key는 aiSummary 하나만 사용합니다. "
    	                  + "설명, 줄바꿈, 불필요한 문장은 모두 금지됩니다."
    	                ),
    		        // 실제 데이터 (핵심)
    		        new InputMessageDto(
    		        		"user",
    		        		buildUserPrompt(
    		        				year,
    		        				month,
    		        				totalSpend,
    		        				impulseSpend,
    		        				impulseRatio,
    		        				topCategory
    		        				)
    		        		)
    		    ))
    		    .build();

        try {
			log.info("[REPORT-AI][REQ] {}", objectMapper.writeValueAsString(req));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
        // OpenAI 호출
        OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);
        log.info("[REPORT-AI][RAW RESPONSE] {}", resp);

        String raw = extractOutputText(resp, "aiSummary");
        String summary = raw == null ? "" : raw.trim();

     // 최종 방어
        if (summary.isBlank()) {
            return "이번 달 소비 내역을 분석했어요. 주요 지출 흐름을 확인해보세요.";
        }
        return summary;
    }
	
	public String summarizeEmotionConsumption(
	        long totalSpend,
	        long impulseSpend,
	        double impulseRatio,
	        String topImpulseCategory
	) {

	    OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
	        .model("gpt-5-nano")
	        .input(List.of(

	            // 역할 정의
	            new InputMessageDto(
	                "developer",
	                "당신은 사용자의 감정 소비(충동 소비) 패턴을 해석하는 분석 AI입니다. "
	              + "심리적 원인이나 개인 감정을 추측해서는 안 됩니다. "
	              + "이미 계산된 수치만을 바탕으로 소비 패턴의 형태와 경향을 설명하세요."
	            ),

	            // 출력 규칙
	            new InputMessageDto(
	                "developer",
	                "출력 규칙: 반드시 JSON 객체 하나만 출력하며, "
	              + "key는 emotionSummary 하나만 사용합니다. "
	              + "평가, 훈계, 단정적인 표현은 금지됩니다."
	            ),

	            // 실제 데이터
	            new InputMessageDto(
	                "user",
	                buildEmotionPrompt(
	                    totalSpend,
	                    impulseSpend,
	                    impulseRatio,
	                    topImpulseCategory
	                )
	            )
	        ))
	        .build();

	    try {
	        log.info("[EMOTION-AI][REQ] {}", objectMapper.writeValueAsString(req));
	    } catch (JsonProcessingException e) {
	        log.warn("[EMOTION-AI] request logging failed", e);
	    }

	    OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);
	    log.info("[EMOTION-AI][RAW RESPONSE] {}", resp);

	    String raw = extractOutputText(resp, "emotionSummary");
	    String summary = raw == null ? "" : raw.trim();

	    if (summary.isBlank()) {
	        return "이번 달 감정 소비는 전체 소비 흐름 속에서 자연스럽게 발생한 것으로 보여요.";
	    }

	    return summary;
	}
	
	public PersonaResult summarizeSpendingPersona(
	        long totalSpend,
	        long impulseSpend,
	        double impulseRatio,
	        String topCategory
	) {

	    OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
	        .model("gpt-5-nano")
	        .input(List.of(

	            new InputMessageDto(
	                "developer",
	                "당신은 사용자의 소비 데이터를 기반으로 소비 성향 유형을 분류하는 분석 AI입니다. "
	              + "이미 계산된 수치만을 사용해 해석해야 하며, 새로운 계산이나 추측은 금지됩니다. "
	              + "아래 유형 중 하나만 선택하세요: "
	              + "계획형 소비자, 소액 반복 소비형, 충동 집중 소비형, 카테고리 편중 소비형, 균형형 소비자"
	            ),

	            new InputMessageDto(
	                "developer",
	                "반드시 JSON 객체 하나만 출력하세요. "
	              + "key는 spendingPersona, personaReason 두 개만 사용하세요."
	            ),

	            new InputMessageDto(
	                "user",
	                buildPersonaPrompt(
	                    totalSpend,
	                    impulseSpend,
	                    impulseRatio,
	                    topCategory
	                )
	            )
	        ))
	        .build();

	    OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);

	    return extractPersona(resp);
	}

	public String summarizeDailyRhythm(
	        long totalCount,
	        String peakSlot,
	        double peakRatio
	) {

	    OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
	        .model("gpt-5-nano")
	        .input(List.of(

	            new InputMessageDto(
	                "developer",
	                "당신은 하루 소비 흐름을 해석하는 분석 AI입니다. "
	              + "이미 계산된 수치를 바탕으로 소비 리듬을 설명하세요."
	            ),

	            new InputMessageDto(
	                "developer",
	                "출력 규칙: JSON 객체 하나만 출력하며 "
	              + "key는 spendingRhythm 하나만 사용합니다."
	            ),

	            new InputMessageDto(
	                "user",
	                """
	                [소비 리듬 데이터]
	                - 하루 총 소비 건수: %d건
	                - 가장 소비가 몰린 시간대: %s
	                - 해당 시간대 소비 비중: %.1f%%
	                """.formatted(totalCount, peakSlot, peakRatio)
	            )
	        ))
	        .build();

	    OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);

	    return extractOutputText(resp, "spendingRhythm");
	}

	public String summarizeDailyDensity(
	        long totalSpend,
	        int count,
	        long maxAmount,
	        String densityType
	) {

	    OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
	        .model("gpt-5-nano")
	        .input(List.of(

	            new InputMessageDto(
	                "developer",
	                "당신은 하루 소비의 구조적 밀도를 해석하는 분석 AI입니다. "
	              + "소액 반복인지, 특정 지출 집중인지 설명하세요."
	            ),

	            new InputMessageDto(
	                "developer",
	                "출력 규칙: JSON 객체 하나만 출력하며 "
	              + "key는 spendingDensity 하나만 사용합니다."
	            ),

	            new InputMessageDto(
	                "user",
	                """
	                [소비 밀도 데이터]
	                - 하루 총 소비 금액: %d원
	                - 소비 건수: %d건
	                - 최대 단일 지출: %d원
	                - 소비 밀도 유형: %s
	                """.formatted(totalSpend, count, maxAmount, densityType)
	            )
	        ))
	        .build();

	    OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);

	    return extractOutputText(resp, "spendingDensity");
	}

	public String summarizeDailyComment(
	        long totalSpend,
	        double impulseRatio
	) {

	    OpenAiResponsesRequest req = OpenAiResponsesRequest.builder()
	        .model("gpt-5-nano")
	        .input(List.of(

	            new InputMessageDto(
	                "developer",
	                "당신은 하루 소비를 정리해주는 마무리 AI입니다. "
	              + "평가나 훈계 없이 부드러운 문장으로 마무리하세요."
	            ),

	            new InputMessageDto(
	                "developer",
	                "출력 규칙: JSON 객체 하나만 출력하며 "
	              + "key는 dailyComment 하나만 사용합니다."
	            ),

	            new InputMessageDto(
	                "user",
	                """
	                [하루 소비 요약]
	                - 총 소비 금액: %d원
	                - 감정 소비 비율: %.1f%%
	                """.formatted(totalSpend, impulseRatio)
	            )
	        ))
	        .build();

	    OpenAiResponsesResponse resp = openAiResponsesCaller.call(req);

	    return extractOutputText(resp, "dailyComment");
	}


	
	// ================= 내부 메서드 =================
	
	private String buildUserPrompt(int year, int month, long totalSpend, long impulseSpend, double impulseRatio, String topCategory) {
		
		return """
				아래는 한 사용자의 월간 소비 분석 결과입니다.
				이 데이터를 바탕으로 사용자에게 보여줄 요약 문장을 작성하세요.
				
				[월 정보]
				- 연도: %d
				- 월: %d
				
				[소비 요약]
				- 총 지출: %d원
				- 충동 지출: %d원
				- 충동 지출 비율: %.1f%%
				- 가장 많이 쓴 카테고리: %s
				
				[출력 규칙]
				- 반드시 JSON만 출력
				- key는 aiSummary 하나만 사용
				- 한국어, 부드러운 안내 톤
				- 1~2문장
				- 위 수치를 그대로 사용하고 새로 계산하지 말 것
				
				출력 예시:
				{
  "aiSummary": "..."
				}
				""".formatted(
						year,
						month,
						totalSpend,
						impulseSpend,
						impulseRatio,
						topCategory != null ? topCategory : "없음"
						);
	}
	
	private String buildEmotionPrompt(
	        long totalSpend,
	        long impulseSpend,
	        double impulseRatio,
	        String topImpulseCategory
	) {

	    return """
	[감정 소비 요약]
	- 총 지출: %d원
	- 감정 소비 금액: %d원
	- 감정 소비 비율: %.1f%%
	- 감정 소비가 가장 많이 발생한 카테고리: %s

	[분석 요청]
	- 위 감정 소비 데이터를 바탕으로 소비 패턴을 해석하세요.
	- 감정 소비가 전체 소비에서 차지하는 성격을 설명하세요.
	- 특정 카테고리에 감정 소비가 집중된 흐름이 있는지 설명하세요.
	- 감정 소비의 형태를 ‘잦은 소액 누적형’ 또는 ‘특정 지출 집중형’ 중 하나로 표현하세요.
	- 다음 달을 위한 부담 없는 관리 제안을 한 문장으로 포함하세요.

	[출력 규칙]
	- 반드시 JSON만 출력
	- key는 emotionSummary 하나만 사용
	""".formatted(
	        totalSpend,
	        impulseSpend,
	        impulseRatio,
	        topImpulseCategory != null ? topImpulseCategory : "없음"
	    );
	}
	
	private String buildPersonaPrompt(
	        long totalSpend,
	        long impulseSpend,
	        double impulseRatio,
	        String topCategory
	) {
	    return """
	[소비 데이터]
	- 총 지출: %d원
	- 충동 지출: %d원
	- 충동 지출 비율: %.1f%%
	- 가장 많이 지출한 카테고리: %s

	[요청]
	- 위 소비 데이터를 바탕으로 소비 성향 유형을 하나 선택하세요.
	- 선택한 이유를 한 문장으로 설명하세요.
	""".formatted(
	        totalSpend,
	        impulseSpend,
	        impulseRatio,
	        topCategory != null ? topCategory : "없음"
	    );
	}
	

	
	private PersonaResult extractPersona(OpenAiResponsesResponse resp) {
	    try {
	        String raw = extractRawJson(resp); // output_text 원문
	        JsonNode node = objectMapper.readTree(raw);

	        return new PersonaResult(
	            node.get("spendingPersona").asText(),
	            node.get("personaReason").asText()
	        );
	    } catch (Exception e) {
	        log.warn("[PERSONA-AI] parse failed", e);
	        return new PersonaResult(
	            "균형형 소비자",
	            "전반적으로 특정 소비 패턴이 과도하게 치우치지 않은 모습이에요."
	        );
	    }
	}

    private String extractOutputText(OpenAiResponsesResponse resp, String key) {
        if (resp == null || resp.getOutput() == null) return null;

        for (OutputItemDto out : resp.getOutput()) {
            if (!"message".equals(out.getType())) continue;
            if (out.getContent() == null) continue;

            for (ContentItemDto c : out.getContent()) {
                if ("output_text".equals(c.getType()) && c.getText() != null && !c.getText().isBlank()) {
                	
                	// Json -> 문자
                	try {
                        JsonNode node = objectMapper.readTree(c.getText());
                        JsonNode summaryNode = node.get(key);
                        return summaryNode != null ? summaryNode.asText() : null;
                    } catch (Exception e) {
                        log.warn("[REPORT-AI] JSON parse failed, raw={}", c.getText(), e);
                        return null;
                    }
                }
            }
        }
        return null;
    }
    
    public String extractRawJson(OpenAiResponsesResponse resp) {
        if (resp == null || resp.getOutput() == null) return null;

        for (OutputItemDto out : resp.getOutput()) {
            if (!"message".equals(out.getType())) continue;
            if (out.getContent() == null) continue;

            for (ContentItemDto c : out.getContent()) {
                if ("output_text".equals(c.getType())
                        && c.getText() != null
                        && !c.getText().isBlank()) {

                    return c.getText(); // JSON 문자열 그대로 반환
                }
            }
        }
        return null;
    }




}
