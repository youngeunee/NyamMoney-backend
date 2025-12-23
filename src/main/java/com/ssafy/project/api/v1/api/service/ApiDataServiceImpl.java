package com.ssafy.project.api.v1.api.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.api.dto.ApiResponse;
import com.ssafy.project.api.v1.api.dto.MerchantItem;
import com.ssafy.project.api.v1.category.service.CategoryService;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiDataServiceImpl implements ApiDataService {

    private static final String BASE_URL = "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInArea";

    private final CategoryService categoryService;
    private final ObjectProvider<VectorStore> vectorStoreProvider;
    private final ObjectMapper objectMapper;

    @Value("${data.go.service-key}")
    private String serviceKey;

    public ApiDataServiceImpl(
            CategoryService categoryService,
            ObjectProvider<VectorStore> vectorStoreProvider,
            ObjectMapper objectMapper) {
        this.categoryService = categoryService;
        this.vectorStoreProvider = vectorStoreProvider;
        this.objectMapper = objectMapper;
    }

    /*
     * =========================
     * 시작 지점
     * =========================
     */
    // @PostConstruct
    @Async
    public void init() {
        log.info("🔥 fetchDataAndStore START");
        fetchDataAndStore();
    }

    /*
     * =========================
     * 메인 적재 로직
     * =========================
     */
    @Override
    public void fetchDataAndStore() {

        int startKey = 10000; // 10000
        int endKey = 10020; // 10508
        int numOfRows = 1000;

        int totalDocs = 0;

        for (int key = startKey; key <= endKey; key++) {

            log.info("🔑 KEY START key={}", key);

            int pageNo = 1;

            log.info("📄 FETCH key={} page={}", key, pageNo);
            ApiResponse first = call(key, pageNo, numOfRows);
            if (first == null || first.getBody() == null) {
                log.warn("⚠️ SKIP key={} (first page null)", key);
                continue;
            }

            int totalCount = first.getBody().getTotalCount();
            int totalPages = (totalCount + numOfRows - 1) / numOfRows;

            log.info("📌 META key={} totalCount={} totalPages={}",
                    key, totalCount, totalPages);

            ingestPage(first, key, pageNo, totalPages);
            totalDocs += safeSize(first);

            for (pageNo = 2; pageNo <= totalPages; pageNo++) {
                log.info("📄 FETCH key={} page={}/{}", key, pageNo, totalPages);

                ApiResponse page = call(key, pageNo, numOfRows);
                if (page == null || page.getBody() == null) {
                    log.warn("⚠️ SKIP key={} page={} (null)", key, pageNo);
                    continue;
                }

                ingestPage(page, key, pageNo, totalPages);
                totalDocs += safeSize(page);
            }

            log.info("✅ KEY DONE key={} totalDocsSoFar={}", key, totalDocs);
        }

        log.info("🎉 INGEST COMPLETE keys=[{}-{}] totalDocs={}",
                startKey, endKey, totalDocs);
    }

    private int safeSize(ApiResponse res) {
        return res.getBody().getItems() == null
                ? 0
                : res.getBody().getItems().size();
    }

    /*
     * =========================
     * API 호출
     * =========================
     */
    private ApiResponse call(int key, int pageNo, int numOfRows) {

        HttpURLConnection conn = null;

        try {
            String finalServiceKey = toQuerySafeServiceKey(
                    serviceKey == null ? "" : serviceKey.trim());

            StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                    .append("?serviceKey=").append(finalServiceKey)
                    .append("&pageNo=").append(pageNo)
                    .append("&numOfRows=").append(numOfRows)
                    .append("&key=").append(key)
                    .append("&type=json");

            URL url = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int code = conn.getResponseCode();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            (code >= 200 && code < 300)
                                    ? conn.getInputStream()
                                    : conn.getErrorStream(),
                            StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);
            br.close();

            if (code < 200 || code >= 300) {
                log.warn("❌ API FAIL key={} page={} code={}", key, pageNo, code);
                return null;
            }

            return objectMapper.readValue(sb.toString(), ApiResponse.class);

        } catch (Exception e) {
            log.warn("❌ API EXCEPTION key={} page={}", key, pageNo, e);
            return null;

        } finally {
            if (conn != null)
                conn.disconnect();
        }
    }

    /*
     * =========================
     * 벡터 적재
     * =========================
     */
    private void ingestPage(
            ApiResponse res,
            int key,
            int pageNo,
            int totalPages) {
        List<MerchantItem> items = res.getBody().getItems();
        if (items == null || items.isEmpty())
            return;

        log.info("📦 INGEST key={} page={}/{} items={}",
                key, pageNo, totalPages, items.size());

        List<Document> docs = new ArrayList<>(items.size());

        for (MerchantItem item : items) {

            String mappedCategory = categoryService.mapCategoryByIndustry(
                    item.getIndsLclsNm(),
                    item.getIndsMclsNm());

            String content = String.format(
                    "%s %s %s",
                    item.getBizesNm(),
                    item.getIndsMclsNm(),
                    item.getIndsSclsNm());

            Map<String, Object> meta = new HashMap<>();
            meta.put("category", mappedCategory);
            meta.put("indsL", item.getIndsLclsNm());
            meta.put("indsM", item.getIndsMclsNm());
            meta.put("indsS", item.getIndsSclsNm());
            meta.put("bizesId", item.getBizesId());

            Document d = new Document(item.getBizesId(), content, meta);
            docs.add(d);
        }

        VectorStore vectorStore = vectorStoreProvider.getObject();

        final int batchSize = 5;
        final long sleepMs = 500L;

        for (int i = 0; i < docs.size(); i += batchSize) {
            int end = Math.min(i + batchSize, docs.size());
            List<Document> batch = docs.subList(i, end);

            long t0 = System.currentTimeMillis();
            vectorStore.add(batch);
            long took = System.currentTimeMillis() - t0;

            log.info("🧠 ADD key={} page={} batchSize={} took={}ms",
                    key, pageNo, batch.size(), took);

            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /*
     * =========================
     * Utils
     * =========================
     */
    private String toQuerySafeServiceKey(String key) throws Exception {
        if (key.contains("%2B") || key.contains("%2F")
                || key.contains("%3D") || key.contains("%25")) {
            return key;
        }
        return URLEncoder.encode(key, StandardCharsets.UTF_8);
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }
}