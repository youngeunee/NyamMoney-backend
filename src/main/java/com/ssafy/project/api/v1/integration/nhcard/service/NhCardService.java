package com.ssafy.project.api.v1.integration.nhcard.service;

import java.time.LocalDate;
import java.util.List;

import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalItem;
import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;

public interface NhCardService {

    /**
     * NH 승인 내역을 조회해 트랜잭션 업서트용 파라미터 목록으로 변환해 반환한다.
     * 저장/분류는 호출 측(transaction/category)에서 수행한다.
     */
    List<NhCardApprovalItem> collect(Long userId, LocalDate fromDate, LocalDate toDate);
}
