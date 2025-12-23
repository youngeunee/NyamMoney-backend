package com.ssafy.project.api.v1.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.openai.service.ReportAiService;
import com.ssafy.project.api.v1.report.dto.CategoryStats;
import com.ssafy.project.api.v1.report.dto.MonthlyReportResponse;
import com.ssafy.project.api.v1.report.mapper.AiReportMapper;

@Service
public class AiReportServiceImpl implements AiReportService {
	private final ReportAiService reportAiService; // ai 서비스
	private final AiReportMapper aiReportMapper; // 매퍼
	public AiReportServiceImpl(ReportAiService reportAiService, AiReportMapper aiReportMapper) {
		this.reportAiService = reportAiService;
		this.aiReportMapper = aiReportMapper;
	}
	
	public MonthlyReportResponse getMonthlyAnalysis(Long userId, Integer year, Integer month) {
		
		 // 기준 연/월 확정
        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        // 조회 기간
        LocalDateTime startAt = LocalDate.of(y, m, 1).atStartOfDay();
        LocalDateTime endAt = startAt.plusMonths(1);

        // Mapper 호출
        List<CategoryStats> stats =
                aiReportMapper.selectMonthlyCategoryStats(userId, startAt, endAt);

        // 월 합계 계산
        long totalSpend = stats.stream().mapToLong(CategoryStats::getTotalSpend).sum();
        long impulseSpend = stats.stream().mapToLong(CategoryStats::getImpulseSpend).sum();

        double impulseRatio = totalSpend == 0
                ? 0.0
                : Math.round((impulseSpend * 1000.0 / totalSpend)) / 10.0;

        // 카테고리별 impulseRatio 계산
        stats.forEach(s -> {
            double ratio = s.getTotalSpend() == 0
                    ? 0.0
                    : Math.round((s.getImpulseSpend() * 1000.0 / s.getTotalSpend())) / 10.0;

            s.setImpulseRatio(ratio);

            if (s.getCategoryName() == null) {
                s.setCategoryName("미분류");
            }
        });

        // topCategory
        String topCategory = stats.isEmpty()
                ? null
                : stats.get(0).getCategoryName();

        // AI 요약
        String aiSummary;
        String emotionSummary;
        try {
            aiSummary = reportAiService.summarizeMonthly(y, m, totalSpend, impulseSpend, impulseRatio, topCategory);
            emotionSummary = reportAiService.summarizeEmotionConsumption(totalSpend, impulseSpend, impulseRatio, topCategory);
            
            
            
        } catch (Exception e) {
            aiSummary = "이번 달 소비 내역을 분석했어요. 주요 지출 흐름을 확인해보세요.";
            emotionSummary = "";
        }

        
        // 8Response
        return new MonthlyReportResponse(
                y, m,
                totalSpend,
                impulseSpend,
                impulseRatio,
                topCategory,
                stats,
                aiSummary, emotionSummary
        );
	}
	
	
}
