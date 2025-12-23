package com.ssafy.project.api.v1.integration.nhcard.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.integration.nhcard.service.NhCardService;
import com.ssafy.project.security.auth.UserPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/integration/nhcard")
public class NhCardController {

    private final NhCardService nhCardService;

    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> sync(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Long userId = principal.getUserId();

        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);

        if (ChronoUnit.DAYS.between(fromDate, toDate) > 92) {
            throw new IllegalArgumentException("조회 기간은 최대 3개월입니다.");
        }

        var params = nhCardService.collect(userId, fromDate, toDate);

        Map<String, Object> res = Map.of(
                "success", true,
                "from", from,
                "to", to,
                "syncedCount", params.size(),
                "items", params
        );

        return ResponseEntity.ok(res);
    }

    private LocalDate parseDate(String ymd) {
        try {
            return LocalDate.parse(ymd, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식은 yyyyMMdd 입니다.");
        }
    }
}
