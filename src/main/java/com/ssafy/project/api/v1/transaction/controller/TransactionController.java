package com.ssafy.project.api.v1.transaction.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.integration.nhcard.dto.TransactionUpsertParam;
import com.ssafy.project.api.v1.integration.nhcard.service.NhCardService;
import com.ssafy.project.api.v1.brand.service.BrandService;
import com.ssafy.project.api.v1.category.service.CategoryService;
import com.ssafy.project.api.v1.openai.service.MerchantCategoryAiService;
import com.ssafy.project.api.v1.follow.dto.FollowStatusResponse;
import com.ssafy.project.api.v1.follow.service.FollowService;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionCreateResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionCursorRequest;
import com.ssafy.project.api.v1.transaction.dto.TransactionDailySummaryItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionDetailResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionItem;
import com.ssafy.project.api.v1.transaction.dto.TransactionSummaryResponse;
import com.ssafy.project.api.v1.transaction.dto.TransactionUpdateRequest;
import com.ssafy.project.api.v1.transaction.service.TransactionService;
import com.ssafy.project.api.v1.user.dto.UserDetailResponse;
import com.ssafy.project.api.v1.user.service.UserService;
import com.ssafy.project.common.dto.CursorPage;
import com.ssafy.project.domain.user.model.ProfileVisibility;
import com.ssafy.project.security.auth.UserPrincipal;

import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/transactions")
@Slf4j
public class TransactionController {
	
	private final TransactionService transactionService;
    private final NhCardService nhCardService;
    private final BrandService brandService;
    private final CategoryService categoryService;
    private final MerchantCategoryAiService merchantCategoryAiService;
    private final UserService userService;
    private final FollowService followService;
    
	public TransactionController(
	        TransactionService transactionService,
	        NhCardService nhCardService,
	        BrandService brandService,
	        CategoryService categoryService,
	        MerchantCategoryAiService merchantCategoryAiService,
            UserService userService,
            FollowService followService
    ) {
		this.transactionService = transactionService;
		this.nhCardService = nhCardService;
		this.brandService = brandService;
		this.categoryService = categoryService;
		this.merchantCategoryAiService = merchantCategoryAiService;
        this.userService = userService;
        this.followService = followService;
	}
	
	@PostMapping
	public ResponseEntity<TransactionCreateResponse> createTransaction(@AuthenticationPrincipal UserPrincipal principal, @RequestBody TransactionCreateRequest req) {
		Long userId = principal.getUserId();
		if (req.getImpulseFlag() == null) {
			req.setImpulseFlag(Boolean.FALSE);
		}
		
		TransactionCreateResponse res = transactionService.createTransaction(userId, req);
		
		return ResponseEntity.ok(res);
	}
	
	@PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionDetailResponse> updateTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long transactionId,
            @RequestBody TransactionUpdateRequest req
    ) {
        Long userId = principal.getUserId();
        TransactionDetailResponse res = transactionService.updateTransaction(userId, transactionId, req);
        return ResponseEntity.ok(res);
    }
	
	@DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long transactionId
    ) {
        Long userId = principal.getUserId();
        transactionService.deleteTransaction(userId, transactionId);
        return ResponseEntity.noContent().build();
    }
	
	@GetMapping("/{transactionId}")
	public ResponseEntity<TransactionDetailResponse> getTransactionDetail(
	        @AuthenticationPrincipal UserPrincipal principal,
	        @PathVariable Long transactionId
	) {
	    Long userId = principal.getUserId();
	    
	    TransactionDetailResponse res = transactionService.getTransactionDetail(userId, transactionId);
	    return ResponseEntity.ok(res);
	}
	
	@GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        Long userId = principal.getUserId();

        // 기본값: to=now, from=to가 속한 달 1일 00:00:00
        LocalDateTime end = (to != null) ? to : LocalDateTime.now();
        LocalDateTime start = (from != null)
                ? from
                : end.toLocalDate().withDayOfMonth(1).atStartOfDay();

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        TransactionSummaryResponse res = transactionService.getSummary(userId, start, end);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/users/{targetUserId}/summary")
    public ResponseEntity<TransactionSummaryResponse> getUserSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long targetUserId,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        Long userId = principal.getUserId();

        // 기본값: to=now, from=해당 월 1일 00:00:00
        LocalDateTime end = (to != null) ? to : LocalDateTime.now();
        LocalDateTime start = (from != null)
                ? from
                : end.toLocalDate().withDayOfMonth(1).atStartOfDay();

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        // 자기 자신은 항상 허용
        if (!userId.equals(targetUserId)) {
            UserDetailResponse target = userService.getUserDetail(targetUserId);
            if (target == null) {
                return ResponseEntity.notFound().build();
            }
            ProfileVisibility visibility = target.getProfileVisibility();
            boolean isPublic = visibility == ProfileVisibility.PUBLIC;
            boolean isProtected = visibility == ProfileVisibility.PROTECTED;

            boolean allowed = isPublic;

            if (isProtected) {
            	log.debug("나 프라이빗 계정이야!");
            	FollowStatusResponse status = followService.getFollowStatus(userId, targetUserId);
                String follow = status != null ? status.getStatus() : null;
                allowed = "ACCEPTED".equalsIgnoreCase(follow);
            }

            if (!allowed) {
            	log.debug("너 못 봐 ");
            	return ResponseEntity.status(403).build();
            }
        }

        TransactionSummaryResponse res = transactionService.getSummary(targetUserId, start, end);
        log.debug("호출 호출: ", res.toString());
        return ResponseEntity.ok(res);
    }
	
    @GetMapping
    public ResponseEntity<CursorPage<TransactionItem>> getTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @ModelAttribute TransactionCursorRequest request
    ) {
        Long userId = principal.getUserId();

        CursorPage<TransactionItem> res = transactionService.getTransactions(userId, request);

        return ResponseEntity.ok(res);
    }
    
    @GetMapping("/daily-summary")
    public ResponseEntity<List<TransactionDailySummaryItem>> getDailySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to
    ) {
        Long userId = principal.getUserId();

        // 기본값: to=now, from=to가 속한 달 1일 00:00:00
        LocalDateTime end = (to != null) ? to : LocalDateTime.now();
        LocalDateTime start = (from != null)
                ? from
                : end.toLocalDate().withDayOfMonth(1).atStartOfDay();

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("from은 to보다 이후일 수 없습니다.");
        }

        List<TransactionDailySummaryItem> res =
                transactionService.getDailySummary(userId, start, end);

        return ResponseEntity.ok(res);
    }
    
    
    @PostMapping("/nh/sync")
    public ResponseEntity<Map<String, Object>> syncNhTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String from,
            @RequestParam String to
    ) {
        Long userId = principal.getUserId();

        // 1) yyyyMMdd 파싱
        LocalDate fromDate = LocalDate.parse(from, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate toDate = LocalDate.parse(to, DateTimeFormatter.BASIC_ISO_DATE);

        // 2) 기간 검증 (최대 3개월)
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("from은 to 이후일 수 없습니다.");
        }
        if (ChronoUnit.DAYS.between(fromDate, toDate) > 92) {
            throw new IllegalArgumentException("조회 기간은 최대 3개월입니다.");
        }

        // 3) 서비스 호출 (NH 호출 + 분류 + upsert 저장)
        int syncedCount = transactionService.syncNhTransactions(userId, fromDate, toDate);

        // 4) 결과만 반환
        Map<String, Object> body = Map.of(
                "success", true,
                "from", from,
                "to", to,
                "syncedCount", syncedCount
        );

        return ResponseEntity.ok(body);
    }

    @GetMapping("/classify")
    public ResponseEntity<Map<String, Object>> classifyCategory(
            @RequestParam String merchantName,
            @RequestParam(defaultValue = "true") boolean allowAi
    ) {
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("merchantName은 필수입니다.");
        }

        String cleaned = merchantName.trim();
        Long categoryId = 10L; // default 기타
        String method = "NONE";

        Long byRule = brandService.findBrand(cleaned);
        if (byRule != null) {
            categoryId = byRule;
            method = "RULE";
        } else {
            Long byVector = categoryService.findVector(cleaned);
            if (byVector != null) {
                categoryId = byVector;
                method = "VECTOR";
            } else if (allowAi) {
                try {
                    String code = merchantCategoryAiService.classifySingleDigitCode(cleaned); // "0"~"9"
                    categoryId = transactionService.mapCodeToCategoryId(code); // 1~10
                    method = "OPENAI";
                } catch (Exception e) {
                    categoryId = 10L;
                    method = "NONE";
                }
            }
        }

        Map<String, Object> body = Map.of(
                "merchantName", cleaned,
                "categoryId", categoryId,
                "method", method
        );

        return ResponseEntity.ok(body);
    }

}
