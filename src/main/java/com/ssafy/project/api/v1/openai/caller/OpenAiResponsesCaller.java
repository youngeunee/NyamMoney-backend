package com.ssafy.project.api.v1.openai.caller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesRequest;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesResponse;

@Component
public class OpenAiResponsesCaller {

    private final RestClient restClient;

    public OpenAiResponsesCaller(
            RestClient.Builder builder,
            @Value("${spring.ai.openai.base-url}") String baseUrl,
            @Value("${ssafy.gms.api-key}") String apiKey
    ) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public OpenAiResponsesResponse call(OpenAiResponsesRequest request) {
        return restClient.post()
                .uri("/responses")
                .body(request)
                .retrieve()
                .body(OpenAiResponsesResponse.class);
    }
}