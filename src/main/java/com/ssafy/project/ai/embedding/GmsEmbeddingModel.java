package com.ssafy.project.ai.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.embedding.EmbeddingResultMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Embedding model implementation that directly calls the SSAFY GMS proxy
 * (https://gms.ssafy.io/gmsapi/api.openai.com/v1/embeddings). This avoids the
 * auto-configured OpenAI client, which expects real OpenAI keys.
 */
@Component("gmsEmbeddingModel")
@Primary
@Slf4j
public class GmsEmbeddingModel extends AbstractEmbeddingModel {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final String apiKey;
    private final String model;

    public GmsEmbeddingModel(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             @Value("${spring.ai.openai.api-key}") String apiKey,
                             @Value("${spring.ai.openai.base-url}") String baseUrl,
                             @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        if (baseUrl.endsWith("/")) {
            this.endpoint = baseUrl + "embeddings";
        }
        else {
            this.endpoint = baseUrl + "/embeddings";
        }
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> inputs = request.getInstructions();
        if (inputs == null || inputs.isEmpty()) {
            return new EmbeddingResponse(Collections.emptyList(), new EmbeddingResponseMetadata());
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", model);
            if (inputs.size() == 1) {
                payload.put("input", inputs.get(0));
            }
            else {
                ArrayNode inputArray = payload.putArray("input");
                inputs.forEach(inputArray::add);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RestClientException("Embedding API failed with status " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            ArrayNode data = (ArrayNode) root.path("data");

            List<Embedding> embeddings = new ArrayList<>();
            for (JsonNode node : data) {
                ArrayNode vectorNode = (ArrayNode) node.path("embedding");
                float[] vector = new float[vectorNode.size()];
                for (int i = 0; i < vectorNode.size(); i++) {
                    vector[i] = (float) vectorNode.get(i).asDouble();
                }
                int index = node.path("index").asInt(embeddings.size());
                embeddings.add(new Embedding(vector, index, EmbeddingResultMetadata.EMPTY));
            }

            EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
            metadata.setModel(root.path("model").asText(model));

            return new EmbeddingResponse(embeddings, metadata);
        }
        catch (Exception e) {
            log.error("Failed to fetch embeddings from SSAFY GMS endpoint", e);
            throw new RestClientException("Failed to fetch embeddings", e);
        }
    }

    @Override
    public float[] embed(Document document) {
        if (document == null) {
            return new float[0];
        }
        // Prefer the text content of the document.
        String text = extractDocumentText(document);
        if (text == null) {
            text = "";
        }
        return embed(text);
    }
    

    private static String extractDocumentText(Document document) {
        // Spring AI Document API differs by version (e.g., getText(), getContent()).
        // Use reflection so this compiles across versions.
        try {
            var m = document.getClass().getMethod("getText");
            Object v = m.invoke(document);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception ignore) {
        }
        try {
            var m = document.getClass().getMethod("getContent");
            Object v = m.invoke(document);
            return v == null ? "" : String.valueOf(v);
        } catch (Exception ignore) {
        }
        // Fallback
        return document.toString();
    }
}
