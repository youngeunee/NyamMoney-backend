package com.ssafy.project.api.v1.report.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.report.dto.DailyReportResponse;
import com.ssafy.project.api.v1.report.dto.MonthlyReportResponse;
import com.ssafy.project.api.v1.report.service.AiReportService;
import com.ssafy.project.security.auth.UserPrincipal;

@RestController
@RequestMapping("/api/v1/ai/report")
public class AiReportController {
	private final AiReportService aiService;
	public AiReportController(AiReportService aiService) {
		this.aiService = aiService;
	}
	
	@GetMapping("/monthly")
	public ResponseEntity<MonthlyReportResponse> getMonthlyAnalysis(
			@AuthenticationPrincipal UserPrincipal principal,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) Integer month
			) {
		Long userId = principal.getUserId();
		
		MonthlyReportResponse response = aiService.getMonthlyAnalysis(userId, year, month);
		
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/daily")
	public ResponseEntity<DailyReportResponse> getDailyReport(
			@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String date
    ) {
		Long userId = principal.getUserId();
        // 기준 날짜 확정
        LocalDate targetDate = (date == null)
                ? LocalDate.now()
                : LocalDate.parse(date, DateTimeFormatter.ISO_DATE);

        // Service 호출
        DailyReportResponse response = aiService.getDailyAnalysis(userId, targetDate);

        // 응답
        return ResponseEntity.ok(response);
    }
}
