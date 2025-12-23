package com.ssafy.project.api.v1.openai.caller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesRequest;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesResponse;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OpenAiResponsesCaller {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAiResponsesCaller(
            RestClient.Builder builder,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${ssafy.gms.api-key}") String apiKey,
            ObjectMapper objectMapper) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.restClient = builder
                .baseUrl(normalizedBaseUrl) // 氚橂摐??.../v1/ (trailing slash ?犾?)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestInterceptor((request, body, execution) -> {
                    log.info("[OPENAI][HTTP] {} {}", request.getMethod(), request.getURI());
                    return execution.execute(request, body);
                })
                .build();
        this.objectMapper = objectMapper;
    }

    public OpenAiResponsesResponse call(OpenAiResponsesRequest request) {
        String rawJson = restClient.post()
                // Use relative path to respect baseUrl (leading slash would drop
                // /gmsapi/api.openai.com/v1)
                .uri("responses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);

        log.info("[OPENAI][RAW JSON] {}", rawJson);

        try {
            return objectMapper.readValue(rawJson, OpenAiResponsesResponse.class);
        } catch (JsonProcessingException e) {
            log.error("[OPENAI][PARSE ERROR] raw={}", rawJson, e);
            return null;
        }
    }
}
