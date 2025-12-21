package com.ssafy.project.api.v1.transaction.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionDailySummaryItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryQuery;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateParam;

@Mapper
public interface TransactionMapper {
	// insert 후 p.transactionId가 채워짐 (useGeneratedKeys)
    int insertTransaction(TransactionCreateParam p);

    // 생성 직후 응답 조회 (resultMap + category join)
    TransactionCreateResponse selectCreateResponseById(@Param("userId") Long userId,
                                                       @Param("transactionId") Long transactionId);
	
    int updateTransaction(TransactionUpdateParam p);

    TransactionDetailResponse selectDetailById(Long userId, Long transactionId);

	int deleteTransaction(@Param("userId") Long userId, 
						  @Param("transactionId") Long transactionId);
	
	TransactionSummaryResponse selectSummary(TransactionSummaryQuery query);

	List<TransactionItem> selectTransactionsCursor(
		    @Param("userId") Long userId,
		    @Param("from") LocalDateTime from,
		    @Param("to") LocalDateTime to,
		    @Param("cursorOccurredAt") LocalDateTime cursorOccurredAt,
		    @Param("cursorTransactionId") Long cursorTransactionId,
		    @Param("limit") int limit
		);

		long countTransactionsByPeriod(
		    @Param("userId") Long userId,
		    @Param("from") LocalDateTime from,
		    @Param("to") LocalDateTime to
		);

		List<TransactionDailySummaryItem> selectDailySummary(TransactionSummaryQuery query);
}
