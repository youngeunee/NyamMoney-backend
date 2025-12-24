package com.ssafy.project.api.v1.report.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.report.dto.CategoryStats;
import com.ssafy.project.api.v1.report.dto.DailyTxStat;

@Mapper
public interface AiReportMapper {

	List<CategoryStats> selectMonthlyCategoryStats(
			@Param("userId") Long userId,
			@Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt
			);

	List<CategoryStats> selectDailyCategoryStats(
			@Param("userId") Long userId,
			@Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt
			);

	List<DailyTxStat> selectDailyTxStats(
			@Param("userId") Long userId,
			@Param("startAt") LocalDateTime startAt,
			@Param("endAt") LocalDateTime endAt
			);

}
