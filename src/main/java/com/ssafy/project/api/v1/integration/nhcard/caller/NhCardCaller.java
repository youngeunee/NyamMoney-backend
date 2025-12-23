package com.ssafy.project.api.v1.integration.nhcard.caller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalRequest;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalResponse;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardRequestHeader;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardResponseHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NhCardCaller {

    private final ObjectMapper objectMapper;

    @Value("${nhcard.approval.url}")
    private String approvalUrl;

    @Value("${nhcard.header.api-nm}")
    private String apiNm;

    @Value("${nhcard.header.iscd}")
    private String iscd;

    @Value("${nhcard.header.fintech-apsno:001}")
    private String fintechApsno;

    @Value("${nhcard.header.api-svc-cd:CardInfo}")
    private String apiSvcCd;

    @Value("${nhcard.header.access-token}")
    private String accessToken;

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter IS_TUNO_PREFIX = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * NH 승인내역 조회 1회 호출
     */
    public NhCardApprovalResponse inquireApprovals(NhCardApprovalRequest request) {
        HttpURLConnection conn = null;

        try {
            // 1) Header 세팅/보정 (이미 header가 들어와도 강제 보정)
            if (request.getHeader() == null) {
                request.setHeader(buildHeader());
            } else {
                sanitizeAndFillHeader(request.getHeader());
            }

            // 2) 요청 JSON 로깅 (AccessToken 마스킹)
//            log.info("[NH][REQ] url={} body={}", approvalUrl, toSafeJson(request));

            // 3) HTTP POST
            URL url = new URL(approvalUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);
            conn.setDoOutput(true);

            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");

            String jsonBody = objectMapper.writeValueAsString(request);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();

            String rawBody = readAll(conn, status);

            // 4) 응답 원문 로깅 (너무 길면 잘라서)
//            log.info("[NH][RES] httpStatus={} body={}", status, truncate(rawBody, 4000));

            if (status < 200 || status >= 300) {
                throw new IllegalStateException("NH API HTTP 오류 status=" + status);
            }

            NhCardApprovalResponse response =
                    objectMapper.readValue(rawBody, NhCardApprovalResponse.class);

            // 5) 업무 응답코드 확인
            if (response.getHeader() != null && response.getHeader().getRpcd() != null) {
                String rpcd = response.getHeader().getRpcd();
                if (!"00000".equals(rpcd)) {
                    // Rsms는 에러 케이스에서 키가 다를 수도 있어 null 가능
                    String rsms = response.getHeader().getRsms();
                    throw new IllegalStateException("NH 업무 오류 rpcd=" + rpcd + " rsms=" + rsms);
                }
            }

            return response;

        } catch (Exception e) {
            throw new IllegalStateException("NH 승인내역 조회 실패: " + e.getMessage(), e);

        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /* =========================
     * Header 생성/보정
     * ========================= */

    private NhCardRequestHeader buildHeader() {
        LocalDateTime now = LocalDateTime.now();

        return NhCardRequestHeader.builder()
                .apiNm(apiNm)
                .tsymd(now.format(YMD))
                .trtm(now.format(HMS))
                .iscd(iscd)
                .fintechApsno(fintechApsno)
                .apiSvcCd(apiSvcCd)
                .isTuno(generateIsTuno(now))
                .accessToken(accessToken)
                .build();
    }

    /**
     * 이미 들어온 header라도, 비어있거나 형식이 이상하면 강제로 채움/교정
     * - 특히 IsTuno: 반드시 yyyyMMddHHmmss + 랜덤6 (총 20자리)
     */
    private void sanitizeAndFillHeader(NhCardRequestHeader h) {
        LocalDateTime now = LocalDateTime.now();

        if (isBlank(h.getApiNm())) h.setApiNm(apiNm);
        if (isBlank(h.getTsymd())) h.setTsymd(now.format(YMD));
        if (isBlank(h.getTrtm())) h.setTrtm(now.format(HMS));
        if (isBlank(h.getIscd())) h.setIscd(iscd);
        if (isBlank(h.getFintechApsno())) h.setFintechApsno(fintechApsno);
        if (isBlank(h.getApiSvcCd())) h.setApiSvcCd(apiSvcCd);
        if (isBlank(h.getAccessToken())) h.setAccessToken(accessToken);

        // 핵심: IsTuno는 "짧게 들어오면" 무조건 교체
        if (!isValidIsTuno(h.getIsTuno())) {
            h.setIsTuno(generateIsTuno(now));
        }
    }

    private boolean isValidIsTuno(String isTuno) {
        // yyyyMMddHHmmss(14) + 6자리 = 20자리 숫자
        return isTuno != null && isTuno.matches("^\\d{20}$");
    }

    /**
     * IsTuno = yyyyMMddHHmmss + 6자리 랜덤
     */
    private String generateIsTuno(LocalDateTime now) {
        String prefix = now.format(IS_TUNO_PREFIX);
        int random = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return prefix + String.format("%06d", random);
    }

    /* =========================
     * Logging helpers
     * ========================= */

    private String toSafeJson(NhCardApprovalRequest req) {
        try {
            // AccessToken 마스킹용 복사
            NhCardApprovalRequest copy = deepCopy(req);

            NhCardRequestHeader h = copy.getHeader();
            if (h != null && h.getAccessToken() != null && !h.getAccessToken().isBlank()) {
                h.setAccessToken("****MASKED****");
            }
            return objectMapper.writeValueAsString(copy);
        } catch (Exception e) {
            return "(failed to serialize safe request)";
        }
    }

    private NhCardApprovalRequest deepCopy(NhCardApprovalRequest req) throws Exception {
        // ObjectMapper로 JSON roundtrip 복사(간단/확실)
        String json = objectMapper.writeValueAsString(req);
        return objectMapper.readValue(json, NhCardApprovalRequest.class);
    }

    private String readAll(HttpURLConnection conn, int status) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8
                ))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...(truncated)";
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
