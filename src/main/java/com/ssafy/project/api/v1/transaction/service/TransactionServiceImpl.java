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
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;
import com.ssafy.project.api.v1.category.service.CategoryService;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalItem;
import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;
import com.ssafy.project.api.v1.integration.nhcard.service.NhCardService;
import com.ssafy.project.api.v1.openai.service.MerchantCategoryAiService;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionCursorRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionDailySummaryItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryQuery;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateParam;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateRequest;
import com.ssafy.project.api.v1.transaction.mapper.TransactionMapper;
import com.ssafy.project.common.dto.CursorPage;
import com.ssafy.project.common.util.CursorUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final NhCardService nhCardService;
    private final MerchantCategoryAiService merchantCategoryAiService;

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    public TransactionServiceImpl(
            TransactionMapper transactionMapper,
            CategoryMapper categoryMapper,
            BrandService brandService,
            CategoryService categoryService,
            NhCardService nhCardService,
            MerchantCategoryAiService merchantCategoryAiService
    ) {
        this.transactionMapper = transactionMapper;
        this.categoryMapper = categoryMapper;
        this.brandService = brandService;
        this.categoryService = categoryService;
        this.nhCardService = nhCardService;
        this.merchantCategoryAiService = merchantCategoryAiService;
    }

    @Override
    public TransactionCreateResponse createTransaction(Long userId, TransactionCreateRequest req) {
        if (req.getImpulseFlag() == null) {
            req.setImpulseFlag(Boolean.FALSE);
        }

        boolean categoryExists = categoryMapper.existsById(req.getCategoryId());
        if (!categoryExists) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다.");
        }

        TransactionCreateParam p = TransactionCreateParam.from(userId, req);

        int affected = transactionMapper.insertTransaction(p);
        if (affected != 1 || p.getTransactionId() == null) {
            throw new IllegalStateException("거래내역 생성에 실패했습니다.");
        }

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

        if (Boolean.TRUE.equals(req.getIsRefund()) && req.getCanceledAt() == null) {
            req.setCanceledAt(LocalDateTime.now());
        }
        if (Boolean.FALSE.equals(req.getIsRefund())) {
            req.setCanceledAt(null);
        }

        TransactionUpdateParam p = TransactionUpdateParam.from(userId, transactionId, req);
        int updated = transactionMapper.updateTransaction(p);

        if (updated != 1) {
            throw new IllegalArgumentException("거래내역을 찾을 수 없습니다.");
        }

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

        int size = req.getSize() == null ? DEFAULT_SIZE : req.getSize();
        if (size < 1) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;

        LocalDateTime to = (req.getTo() != null) ? req.getTo() : LocalDateTime.now();
        LocalDateTime from = (req.getFrom() != null)
                ? req.getFrom()
                : to.toLocalDate().withDayOfMonth(1).atStartOfDay();

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        LocalDateTime cursorOccurredAt = null;
        Long cursorTransactionId = null;

        if (req.getCursor() != null && !req.getCursor().isBlank()) {
            CursorUtil.Cursor c = CursorUtil.parse(req.getCursor());
            cursorOccurredAt = c.createdAt();
            cursorTransactionId = c.id();
        }

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

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            TransactionItem last = rows.get(rows.size() - 1);
            nextCursor = CursorUtil.format(last.getOccurredAt(), last.getTransactionId());
        }

        long totalCount = transactionMapper.countTransactionsByPeriod(userId, from, to);
        return new CursorPage<>(rows, nextCursor, hasNext, totalCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDailySummaryItem> getDailySummary(Long userId, LocalDateTime from, LocalDateTime to) {
        TransactionSummaryQuery q = new TransactionSummaryQuery(userId, from, to);
        return transactionMapper.selectDailySummary(q);
    }

    /**
     * ✅ 실제 운영용: NH 수집 + 적재
     * - 수집 실패/기간 이슈가 있어도, 적재 로직은 ingestNhApprovalItems로 분리되어 테스트 가능
     */
    @Override
    @Transactional
    public int syncNhTransactions(Long userId, LocalDate fromDate, LocalDate toDate) {
        List<NhCardApprovalItem> items = nhCardService.collect(userId, fromDate, toDate);
        return ingestNhApprovalItems(userId, items);
    }

    /**
     * ✅ 테스트/운영 공용: "적재(insert) + 분류(RULE/VECTOR/OPENAI)"만 수행
     * - 더미 NhCardApprovalItem 리스트를 만들어 이 메서드만 호출하면 검증 끝
     */
    @Transactional
    @Override
    public int ingestNhApprovalItems(Long userId, List<NhCardApprovalItem> items) {

        if (items == null || items.isEmpty()) return 0;

        final String SOURCE = "nhcard";

        List<String> authNos = items.stream()
                .map(NhCardApprovalItem::getCardAthzNo)
                .filter(no -> no != null && !no.isBlank())
                .distinct()
                .toList();

        Set<String> existing = new HashSet<>(
                authNos.isEmpty()
                        ? List.of()
                        : transactionMapper.findExistingNhAuthNos(userId, SOURCE, authNos)
        );

        Map<String, CategoryPick> categoryCache = new HashMap<>();

        int savedCount = 0;

        for (NhCardApprovalItem it : items) {

            String authNo = it.getCardAthzNo();
            if (authNo == null || authNo.isBlank()) continue;
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
                    Long categoryId = brandService.findBrand(merchantName);
                    if (categoryId != null) {
                        pick = new CategoryPick(categoryId, "RULE");
                    } else {
                        categoryId = categoryService.findVector(merchantName);
                        if (categoryId != null) {
                            pick = new CategoryPick(categoryId, "VECTOR");
                        } else {
                            log.info("[CAT] fallback=OPENAI merchantRaw='{}'", merchantName);

                            try {
                                String code = merchantCategoryAiService.classifySingleDigitCode(merchantName); // "0"~"9"
                                Long aiCategoryId = mapCodeToCategoryId(code); // 1~10

                                log.info("[CAT] openaiResult code='{}' categoryId={} merchantRaw='{}'",
                                        code, aiCategoryId, merchantName);

                                pick = new CategoryPick(aiCategoryId, "OPENAI");
                            } catch (Exception e) {
                                log.error("[CAT] openaiFailed merchantRaw='{}' msg={}", merchantName, e.getMessage(), e);
                                pick = new CategoryPick(10L, "NONE");
                            }
                        }
                    }
                    categoryCache.put(merchantName, pick);
                }

                p.setCategoryId(pick.categoryId());
                p.setCategoryMethod(pick.method());
            }

            savedCount += transactionMapper.insertNhCardTransaction(p);
            existing.add(authNo);

            log.info("[TX] saved authNo={} merchant='{}' categoryId={} method={}",
                    authNo, merchantName, p.getCategoryId(), p.getCategoryMethod());
        }

        return savedCount;
    }

    @Override
	public Long mapCodeToCategoryId(String code) {
        if (code == null) return 10L;
        String c = code.trim();
        if (!c.matches("^[0-9]$")) return 10L;
        if ("0".equals(c)) return 10L;   // 기타
        return Long.parseLong(c);        // 1~9
    }

    private record CategoryPick(Long categoryId, String method) {}
    
    @Override
    public TransactionUpsertParam mapToUpsertParam(NhCardApprovalItem it, Long userId) {
        TransactionUpsertParam p = new TransactionUpsertParam();

        p.setUserId(userId);

        String d = it.getTrdd();
        String t = it.getTxtm();
        if (t == null || t.length() < 6) {
            t = String.format("%6s", t == null ? "" : t).replace(' ', '0');
        }
        p.setOccurredAt(LocalDateTime.parse(d + t, DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

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

        String amsl = it.getAmslKnd();
        if ("1".equals(amsl)) p.setSalesType("ONETIME");
        else if ("2".equals(amsl)) p.setSalesType("INSTALLMENT");
        else if ("3".equals(amsl)) p.setSalesType("CASH");
        else if ("6".equals(amsl)) p.setSalesType("FOREIGN_ONETIME");
        else if ("7".equals(amsl)) p.setSalesType("FOREIGN_INSTALLMENT");
        else if ("8".equals(amsl)) p.setSalesType("FOREIGN_CASH");

        if (it.getTris() != null && !"00".equals(it.getTris())) {
            p.setInstallmentMonths(Integer.parseInt(it.getTris()));
        }

        p.setIsRefund("1".equals(it.getCcyn()) ? 1 : 0);

        if (it.getCnclYmd() != null && !it.getCnclYmd().isBlank()) {
            p.setCanceledAt(LocalDate.parse(it.getCnclYmd(), DateTimeFormatter.ofPattern("yyyyMMdd")).atStartOfDay());
        }

        p.setCategoryId(null);
        p.setCategoryMethod(null);

        return p;
    }
}