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
                .baseUrl(baseUrl) // 반드시 .../v1
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public OpenAiResponsesResponse call(OpenAiResponsesRequest request) {
        return restClient.post()
                // Use relative path to respect baseUrl (leading slash would drop /gmsapi/api.openai.com/v1)
                .uri("responses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OpenAiResponsesResponse.class);
    }
}
