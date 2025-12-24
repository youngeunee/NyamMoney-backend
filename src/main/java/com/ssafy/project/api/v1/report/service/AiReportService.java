package com.ssafy.project.api.v1.report.service;

import java.time.LocalDate;

import com.ssafy.project.api.v1.report.dto.DailyReportResponse;
import com.ssafy.project.api.v1.report.dto.MonthlyReportResponse;

public interface AiReportService {

	MonthlyReportResponse getMonthlyAnalysis(Long userId, Integer year, Integer month);

	DailyReportResponse getDailyAnalysis(Long userId, LocalDate date);

}
