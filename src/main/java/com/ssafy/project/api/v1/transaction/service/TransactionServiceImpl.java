package com.ssafy.project.api.v1.transaction.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.brand.service.BrandService;
import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;
import com.ssafy.project.api.v1.category.service.CategoryService;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalItem;
import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;
import com.ssafy.project.api.v1.integration.nhcard.service.NhCardService;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionCursorRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionDailySummaryItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionDto;
import com.ssafy.project.api.v1.transaction.dto.TransactionItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryQuery;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateRequest;
import com.ssafy.project.api.v1.transaction.mapper.TransactionMapper;
import com.ssafy.project.common.dto.CursorPage;
import com.ssafy.project.common.util.CursorUtil;

@Service
public class TransactionServiceImpl implements TransactionService {
	
	private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final NhCardService nhCardService;
    
	private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    
    public TransactionServiceImpl(TransactionMapper transactionMapper, CategoryMapper categoryMapper, BrandService brandService, CategoryService categoryService, NhCardService nhCardService) {
    	this.transactionMapper = transactionMapper;
    	this.categoryMapper = categoryMapper;
    	this.brandService = brandService;
    	this.categoryService = categoryService;
    	this.nhCardService = nhCardService;
    }
	
    @Override
    public TransactionCreateResponse createTransaction(Long userId, TransactionCreateRequest req) {
    	if (req.getImpulseFlag() == null) {
    		req.setImpulseFlag(Boolean.FALSE);
    	}

        // 1) categoryId 존재 검증 (선택: FK가 있으면 DB가 막지만, 메시지/검증을 위해 둠)
        boolean categoryExists = categoryMapper.existsById(req.getCategoryId());
        System.out.println("DEBUG categoryId = " + req.getCategoryId());
        if (!categoryExists) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        // 2) Request -> Param (Insert 전용)
        TransactionCreateParam p = TransactionCreateParam.from(userId, req);

        // 3) insert (generated key가 p.transactionId에 채워짐)
        int affected = transactionMapper.insertTransaction(p);
        if (affected != 1 || p.getTransactionId() == null) {
            throw new IllegalStateException("거래내역 생성에 실패했습니다.");
        }

        // 4) 방금 생성한 row 조회 (resultMap으로 categoryItem까지 포함)
        TransactionCreateResponse res =
                transactionMapper.selectCreateResponseById(userId, p.getTransactionId());

        if (res == null) {
            throw new IllegalStateException("거래내역 생성 후 조회에 실패했습니다.");
        }

        return res;
    }

    @Override
    public TransactionDetailResponse updateTransaction(Long userId, Long transactionId, TransactionUpdateRequest req) {

        if (req.getCategoryId() != null) {
            boolean exists = categoryMapper.existsById(req.getCategoryId());
            if (!exists) {
                throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
            }
        }

        // 환불 체크하면 그냥 지금 시간으로 넣기
        if (Boolean.TRUE.equals(req.getIsRefund()) && req.getCanceledAt() == null) {
            req.setCanceledAt(LocalDateTime.now());
        }
        
        if (Boolean.FALSE.equals(req.getIsRefund())) {
            req.setCanceledAt(null);
        }

        // 업데이트하기 
        TransactionUpdateParam p = TransactionUpdateParam.from(userId, transactionId, req);
        int updated = transactionMapper.updateTransaction(p);

        if (updated != 1) {
            throw new IllegalArgumentException("거래내역을 찾을 수 없습니다.");
        }

        // 다시 조회해서 Return
        TransactionDetailResponse res = transactionMapper.selectDetailById(userId, transactionId);
        if (res == null) {
            throw new IllegalStateException("수정 후 조회에 실패했습니다.");
        }
        return res;
    }
    
    @Override
    public void deleteTransaction(Long userId, Long transactionId) {

        int updated = transactionMapper.deleteTransaction(userId, transactionId);

        if (updated != 1) {
            throw new IllegalArgumentException("삭제할 거래내역을 찾을 수 없습니다.");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionDetail(Long userId, Long transactionId) {
        TransactionDetailResponse res = transactionMapper.selectDetailById(userId, transactionId);
        if (res == null) {
            throw new IllegalArgumentException("거래내역을 찾을 수 없습니다.");
        }
        return res;
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummary(Long userId, LocalDateTime from, LocalDateTime to) {
        TransactionSummaryQuery q = new TransactionSummaryQuery(userId, from, to);
        return transactionMapper.selectSummary(q);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CursorPage<TransactionItem> getTransactions(Long userId, TransactionCursorRequest req) {

        // 1) size 결정
        int size = req.getSize() == null ? DEFAULT_SIZE : req.getSize();
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;

        // 2) 기간 기본값
        LocalDateTime to = (req.getTo() != null) ? req.getTo() : LocalDateTime.now();
        LocalDateTime from = (req.getFrom() != null) ? req.getFrom() : to.toLocalDate().withDayOfMonth(1).atStartOfDay();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        // 3) cursor 파싱 (없으면 null)
        LocalDateTime cursorOccurredAt = null;
        Long cursorTransactionId = null;

        if (req.getCursor() != null && !req.getCursor().isBlank()) {
            CursorUtil.Cursor c = CursorUtil.parse(req.getCursor());
            cursorOccurredAt = c.createdAt(); // CursorUtil이 createdAt 필드명을 쓰고 있으면 그대로
            cursorTransactionId = c.id();
        }

        // 4) size + 1 로 조회해서 hasNext 판단
        List<TransactionItem> rows = transactionMapper.selectTransactionsCursor(
                userId,
                from,
                to,
                cursorOccurredAt,
                cursorTransactionId,
                size + 1
        );

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        // 5) nextCursor 계산 (마지막 요소 기준)
        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            TransactionItem last = rows.get(rows.size() - 1);
            nextCursor = CursorUtil.format(last.getOccurredAt(), last.getTransactionId());
        }

        // 6) totalCount (기간 기준이므로 count도 기간 조건으로 맞추는 게 자연스럽습니다)
        long totalCount = transactionMapper.countTransactionsByPeriod(userId, from, to);

        return new CursorPage<>(rows, nextCursor, hasNext, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDailySummaryItem> getDailySummary(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        TransactionSummaryQuery q = new TransactionSummaryQuery(userId, from, to);
        return transactionMapper.selectDailySummary(q);
    }

    @Override
    @Transactional
    public int syncNhTransactions(Long userId, LocalDate fromDate, LocalDate toDate) {

        // 1) NH 승인내역 수집
        List<NhCardApprovalItem> items = nhCardService.collect(userId, fromDate, toDate);
        if (items == null || items.isEmpty()) return 0;

        final String SOURCE = "nhcard";

        // 2) authNo 목록 뽑기 (null/blank 제외)
        List<String> authNos = items.stream()
            .map(NhCardApprovalItem::getCardAthzNo)
            .filter(no -> no != null && !no.isBlank())
            .distinct()
            .toList();

        // 3) 이미 DB에 있는 authNo를 한 번에 조회
        Set<String> existing = authNos.isEmpty()
            ? Set.of()
            : new HashSet<>(transactionMapper.findExistingNhAuthNos(userId, SOURCE, authNos));

        // 4) (선택) merchantName 기준 분류 결과를 sync 1회 동안만 메모리 캐시
        Map<String, CategoryPick> categoryCache = new HashMap<>();

        int savedCount = 0;

        for (NhCardApprovalItem it : items) {

            String authNo = it.getCardAthzNo();
            if (authNo == null || authNo.isBlank()) {
                // authNo가 없으면 유니크키로 중복판정이 안됨 → 정책 결정 필요
                // 일단 스킵하거나, 다른 키로 중복판정(occurredAt+amount+merchant) 등을 쓰는 방식으로 가야 함
                continue;
            }

            // 5) 이미 있으면 스킵
            if (existing.contains(authNo)) continue;

            TransactionUpsertParam p = mapToUpsertParam(it, userId);
            p.setSource(SOURCE);
            p.setCardAuthNo(authNo);

            String merchantName = p.getMerchantNameRaw();
            if (merchantName == null || merchantName.isBlank()) {
                p.setCategoryId(10L);
                p.setCategoryMethod("NONE");
            } else {
                CategoryPick pick = categoryCache.get(merchantName);

                if (pick == null) {
                    // 6) RULE → VECTOR → NONE
                    Long categoryId = brandService.findBrand(merchantName);
                    if (categoryId != null) {
                        pick = new CategoryPick(categoryId, "RULE");
                    } else {
                        categoryId = categoryService.findVector(merchantName);
                        if (categoryId != null) pick = new CategoryPick(categoryId, "VECTOR");
                        else pick = new CategoryPick(10L, "NONE");
                    }
                    categoryCache.put(merchantName, pick);
                }

                p.setCategoryId(pick.categoryId());
                p.setCategoryMethod(pick.method());
            }

            // 7) insert만 수행
            savedCount += transactionMapper.insertNhCardTransaction(p);

            // 8) 이번 실행 중 중복 방지
            existing.add(authNo);
        }

        return savedCount;
    }

    private record CategoryPick(Long categoryId, String method) {}


    private TransactionUpsertParam mapToUpsertParam(
            NhCardApprovalItem it,
            Long userId
    ) {
        TransactionUpsertParam p = new TransactionUpsertParam();

        p.setUserId(userId);

        // occurredAt
        String d = it.getTrdd();
        String t = it.getTxtm();
        if (t == null || t.length() < 6) {
            t = String.format("%6s", t == null ? "" : t).replace(' ', '0');
        }
        p.setOccurredAt(LocalDateTime.parse(d + t,
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // amount
        long amount = 0L;
        if (it.getUsam() != null && !it.getUsam().isBlank()) {
            amount = Long.parseLong(it.getUsam().trim());
        }
        p.setAmount(amount);

        p.setTxType("EXPENSE");
        p.setMerchantNameRaw(it.getAfstNm());
        p.setPaymentMethod("CARD");
        p.setSource("nhcard");

        p.setCardAuthNo(it.getCardAthzNo());

        // salesType
        String amsl = it.getAmslKnd();
        if ("1".equals(amsl)) p.setSalesType("ONETIME");
        else if ("2".equals(amsl)) p.setSalesType("INSTALLMENT");
        else if ("3".equals(amsl)) p.setSalesType("CASH");
        else if ("6".equals(amsl)) p.setSalesType("FOREIGN_ONETIME");
        else if ("7".equals(amsl)) p.setSalesType("FOREIGN_INSTALLMENT");
        else if ("8".equals(amsl)) p.setSalesType("FOREIGN_CASH");

        // installment
        if (it.getTris() != null && !"00".equals(it.getTris())) {
            p.setInstallmentMonths(Integer.parseInt(it.getTris()));
        }

        // refund
        p.setIsRefund("1".equals(it.getCcyn()) ? 1 : 0);

        // canceledAt
        if (it.getCnclYmd() != null && !it.getCnclYmd().isBlank()) {
            p.setCanceledAt(LocalDate.parse(it.getCnclYmd(),
                    DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay());
        }

        p.setCategoryId(null);
        p.setCategoryMethod(null);

        return p;
    }


}
