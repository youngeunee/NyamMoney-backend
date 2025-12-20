package com.ssafy.project.redis.vector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * NOTE:
 * - This class name stays `RedisVectorStore` only because the file already
 * exists with that name.
 * - We use it as a container for a GMS-compatible EmbeddingModel
 * implementation.
 * - The goal is to send the embedding request in the same shape as the GMS
 * docs/curl example:
 * POST {baseUrl}/v1/embeddings
 * Authorization: Bearer {GMS_KEY}
 * Content-Type: application/json
 * {"model":"text-embedding-3-small","input":["...","..."]}
 */
public class RedisVectorStore {

    /**
     * Low-level client that calls the GMS OpenAI proxy embedding endpoint.
     */
    public static class GmsEmbeddingClient {
        private final String apiKey;
        private final String baseUrl;
        private final ObjectMapper objectMapper;

        public GmsEmbeddingClient(String apiKey, String baseUrl, ObjectMapper objectMapper) {
            this.apiKey = apiKey;
            this.baseUrl = baseUrl;
            this.objectMapper = objectMapper;
        }

        public List<float[]> embeddings(String model, List<String> inputs) {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException(
                        "GMS api key is empty. Check spring.ai.openai.api-key in application.properties");
            }
            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException(
                        "GMS base url is empty. Check spring.ai.openai.base-url in application.properties");
            }
            if (inputs == null || inputs.isEmpty()) {
                return List.of();
            }

            HttpURLConnection conn = null;
            try {
                String endpoint = normalizeBaseUrl(baseUrl) + "/v1/embeddings";
                // DEBUG: print endpoint without leaking the key
                System.out.println(
                        "[GMS][embeddings] endpoint=" + endpoint + ", model=" + model + ", inputs=" + inputs.size());
                URL url = new URL(endpoint);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // ✅ Same headers as docs
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
                conn.setRequestProperty("Accept", "application/json");
                // Some proxies are picky about UA / connection
                conn.setRequestProperty("User-Agent", "curl/8.0");
                conn.setRequestProperty("Connection", "close");

                // ✅ Same body shape as docs (input can be string or array; we always use array)
                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("input", inputs);

                byte[] payload = objectMapper.writeValueAsBytes(body);
                System.out.println("[GMS][embeddings] payloadBytes=" + payload.length);
                // Ensure we are NOT relying on chunked transfer; set fixed length.
                conn.setFixedLengthStreamingMode(payload.length);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                    os.flush();
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();

                String resp;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null)
                        sb.append(line);
                    resp = sb.toString();
                }

                if (code < 200 || code >= 300) {
                    // Print short preview to help debug HTML/proxy errors
                    String preview = resp;
                    if (preview != null && preview.length() > 300) {
                        preview = preview.substring(0, 300) + "...";
                    }
                    System.out.println("[GMS][embeddings] ERROR http=" + code + ", bodyPreview=" + preview);
                    throw new IllegalStateException("GMS embeddings failed. http=" + code + ", body=" + resp);
                }

                // Expected response (OpenAI-compatible): { data: [ { embedding: [...] }, ... ]
                // }
                Map<String, Object> json = objectMapper.readValue(resp, new TypeReference<>() {
                });
                Object dataObj = json.get("data");
                if (!(dataObj instanceof List<?> dataList)) {
                    throw new IllegalStateException("Unexpected embeddings response: missing 'data'");
                }

                List<float[]> vectors = new ArrayList<>(dataList.size());
                for (Object rowObj : dataList) {
                    if (!(rowObj instanceof Map<?, ?> row)) {
                        throw new IllegalStateException("Unexpected embeddings response row: " + rowObj);
                    }
                    Object embObj = row.get("embedding");
                    if (!(embObj instanceof List<?> embList)) {
                        throw new IllegalStateException("Unexpected embeddings response: missing 'embedding'");
                    }
                    float[] vec = new float[embList.size()];
                    for (int i = 0; i < embList.size(); i++) {
                        Object v = embList.get(i);
                        // Jackson may deserialize numbers as Integer/Double
                        if (v instanceof Number n) {
                            vec[i] = n.floatValue();
                        } else {
                            vec[i] = Float.parseFloat(String.valueOf(v));
                        }
                    }
                    vectors.add(vec);
                }
                return vectors;

            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                if (conn != null)
                    conn.disconnect();
            }
        }

        private static String normalizeBaseUrl(String baseUrl) {
            // Supports either:
            // - https://gms.ssafy.io/gmsapi/api.openai.com
            // - https://gms.ssafy.io/gmsapi/api.openai.com/
            if (baseUrl.endsWith("/"))
                return baseUrl.substring(0, baseUrl.length() - 1);
            return baseUrl;
        }
    }

    /**
     * EmbeddingModel implementation that uses GmsEmbeddingClient.
     * This bypasses Spring AI's internal OpenAI client so the request matches the
     * docs/curl shape.
     */
    public static class GmsEmbeddingModel implements EmbeddingModel {
        private final GmsEmbeddingClient client;
        private final String model;

        public GmsEmbeddingModel(GmsEmbeddingClient client, String model) {
            this.client = client;
            this.model = model;
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            // This project’s Spring AI version uses primitive embedding methods
            // (embed(String), embed(List<String>), embed(Document)).
            // VectorStore flows call those methods directly.
            // Implementing EmbeddingResponse here would require using Embedding
            // constructors
            // that are not available in this version.
            throw new UnsupportedOperationException(
                    "call(EmbeddingRequest) is not used; use embed(String/List/Document) instead.");
        }

        @Override
        public List<float[]> embed(List<String> texts) {
            return client.embeddings(model, texts);
        }

        @Override
        public float[] embed(String text) {
            List<float[]> vectors = client.embeddings(model, List.of(text));
            if (vectors.isEmpty()) {
                return new float[0];
            }
            return vectors.get(0);
        }

        @Override
        public float[] embed(Document document) {
            if (document == null) {
                return new float[0];
            }
            String text = extractDocumentText(document);
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
}
