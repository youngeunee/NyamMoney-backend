package com.ssafy.project.api.v1.report.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.openai.dto.OpenAiResponsesResponse;
import com.ssafy.project.api.v1.openai.service.ReportAiService;
import com.ssafy.project.api.v1.report.dto.CategoryStats;
import com.ssafy.project.api.v1.report.dto.DailyReportResponse;
import com.ssafy.project.api.v1.report.dto.DailyTxStat;
import com.ssafy.project.api.v1.report.dto.MonthlyReportResponse;
import com.ssafy.project.api.v1.report.dto.PersonaResult;
import com.ssafy.project.api.v1.report.mapper.AiReportMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiReportServiceImpl implements AiReportService {
	
	private final ReportAiService reportAiService; // ai 서비스
	private final AiReportMapper aiReportMapper; // 매퍼
	private final ObjectMapper objectMapper;
	public AiReportServiceImpl(ReportAiService reportAiService,
			AiReportMapper aiReportMapper,
			ObjectMapper objectMapper) {
		this.reportAiService = reportAiService;
		this.aiReportMapper = aiReportMapper;
		this.objectMapper = objectMapper;
		
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
        PersonaResult persona;
        try {
            aiSummary = reportAiService.summarizeMonthly(y, m, totalSpend, impulseSpend, impulseRatio, topCategory);
            emotionSummary = reportAiService.summarizeEmotionConsumption(totalSpend, impulseSpend, impulseRatio, topCategory);
            persona = reportAiService.summarizeSpendingPersona(totalSpend, impulseSpend, impulseRatio, topCategory);
        } catch (Exception e) {
            aiSummary = "이번 달 소비 내역을 분석했어요. 주요 지출 흐름을 확인해보세요.";
            emotionSummary = "이번 달에는 감정 소비가 특정 패턴으로 두드러지기보다는 전반적인 소비 흐름에 자연스럽게 포함되어 있었어요.";
            persona = new PersonaResult(
            	    "균형형 소비자",
            	    "소비가 한쪽으로 크게 치우치지 않고 비교적 고르게 나타났어요."
            	);
        }        
        // Response
        return new MonthlyReportResponse(
                y, m,
                totalSpend,
                impulseSpend,
                impulseRatio,
                topCategory,
                stats,
                aiSummary, emotionSummary, persona
        );
	}


	public DailyReportResponse getDailyAnalysis(Long userId, LocalDate date) {
		// 조회 기간 계산
		LocalDateTime startAt = date.atStartOfDay();
		LocalDateTime endAt = startAt.plusDays(1);

	    // Mapper 호출
		List<CategoryStats> stats =
		        aiReportMapper.selectDailyCategoryStats(userId, startAt, endAt);
		List<DailyTxStat> txList =
		        aiReportMapper.selectDailyTxStats(userId, startAt, endAt);

	    // Service 계산 로직 (리듬 / 밀도 / 비율)
		// 총액/감정 소비
		long totalSpend = txList.stream()
		        .mapToLong(DailyTxStat::getAmount)
		        .sum();
		long impulseSpend = txList.stream()
		        .filter(DailyTxStat::isImpulse)
		        .mapToLong(DailyTxStat::getAmount)
		        .sum();
		double impulseRatio = totalSpend == 0
		        ? 0.0
		        : Math.round((impulseSpend * 1000.0 / totalSpend)) / 10.0;
		
		// 소비 리듬
		Map<String, Long> slotCount = txList.stream()
			    .collect(Collectors.groupingBy(
			        tx -> toTimeSlot(tx.getHour()),
			        Collectors.counting()
			    ));

			long totalCount = txList.size();

			Map.Entry<String, Long> peak =
			    slotCount.entrySet().stream()
			        .max(Map.Entry.comparingByValue())
			        .orElse(null);

			String peakSlot = peak != null ? peak.getKey() : null;
			double peakRatio = peak == null
			        ? 0.0
			        : Math.round((peak.getValue() * 1000.0 / totalCount)) / 10.0;
			
			// 소비 밀도
			int count = txList.size();

			long maxAmount = txList.stream()
			        .mapToLong(DailyTxStat::getAmount)
			        .max()
			        .orElse(0);

			long avgAmount = count == 0 ? 0 : totalSpend / count;

			String densityType;
			if (maxAmount > totalSpend * 0.5) {
			    densityType = "집중형";
			} else if (count >= 5 && avgAmount < 10000) {
			    densityType = "소액 반복형";
			} else {
			    densityType = "혼합형";
			}

	    // AI 호출
			String spendingRhythm;
			String spendingDensity;
			String dailyComment;

			try {
			    spendingRhythm = reportAiService.summarizeDailyRhythm(
			            totalCount, peakSlot, peakRatio);
			    spendingDensity = reportAiService.summarizeDailyDensity(
			            totalSpend, count, maxAmount, densityType);
			    dailyComment = reportAiService.summarizeDailyComment(
			            totalSpend, impulseRatio);
			} catch (Exception e) {
			    spendingRhythm = "오늘의 소비는 특정 시간대에 크게 치우치지 않은 흐름이었어요.";
			    spendingDensity = "소비가 한 가지 형태로 뚜렷하게 나타나지는 않았어요.";
			    dailyComment = "오늘의 소비를 기록하고 돌아본 것만으로도 충분한 하루예요.";
			}

	    // Response 생성
			return new DailyReportResponse(
			        date.toString(),
			        totalSpend,
			        impulseSpend,
			        impulseRatio,
			        stats,
			        spendingRhythm,
			        spendingDensity,
			        dailyComment
			);
	}
	
	private String toTimeSlot(int hour) {
	    if (hour < 6) return "새벽";
	    if (hour < 12) return "아침";
	    if (hour < 18) return "오후";
	    return "저녁";
	}

	
}
