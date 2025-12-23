package com.ssafy.project.api.v1.integration.nhcard.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.integration.nhcard.caller.NhCardCaller;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalItem;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalRequest;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NhCardServiceImpl implements NhCardService {

    private final NhCardCaller nhCardCaller;

    // yyyyMMdd
    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int PAGE_SIZE = 15;
    private static final String IOUS_DSNC_DOMESTIC = "1";

    // finCard는 “고정”이라고 했으니 properties 주입 추천
    // 지금은 일단 필드로 둡니다.
    private final String finCard = "00829101234560000112345678919";

    public NhCardServiceImpl(NhCardCaller nhCardCaller) {
        this.nhCardCaller = nhCardCaller;
    }

    /**
     * NH 승인내역 “조회 전용” 메서드
     * - 페이징(CtntDataYn) 끝까지 따라가서 REC를 모두 모아 반환
     * - 저장/카테고리/변환 로직 없음
     */
    @Override
    public List<NhCardApprovalItem> collect(Long userId, LocalDate fromDate, LocalDate toDate) {
        validatePeriod(fromDate, toDate);

        int pageNo = 1;
        List<NhCardApprovalItem> all = new ArrayList<>();

        while (true) {
            NhCardApprovalRequest req = buildRequest(fromDate, toDate, pageNo);

            log.info("[NH][COLLECT] request pageNo={} from={} to={} dmcnt={}",
                    pageNo, req.getInsymd(), req.getIneymd(), req.getDmcnt());

            NhCardApprovalResponse res = nhCardCaller.inquireApprovals(req);

            List<NhCardApprovalItem> items = (res == null) ? null : res.getItems();
            int fetched = (items == null) ? 0 : items.size();

            log.info("[NH][COLLECT] response pageNo={} fetched={} ctntDataYn={}",
                    pageNo, fetched, (res == null ? null : res.getCtntDataYn()));

            if (items == null || items.isEmpty()) {
                break;
            }

            all.addAll(items);

            if (!hasNext(res)) {
                break;
            }
            pageNo++;
        }

        log.info("[NH][COLLECT] done total={} from={} to={}",
                all.size(), fromDate, toDate);

        return all;
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from/to는 필수입니다.");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from은 to 이후일 수 없습니다.");
        }
        // 3개월 제한은 컨트롤러에서 검증한다면 여기서는 생략 가능
    }

    private NhCardApprovalRequest buildRequest(LocalDate fromDate, LocalDate toDate, int pageNo) {
        return NhCardApprovalRequest.builder()
                .finCard(finCard)
                .iousDsnc(IOUS_DSNC_DOMESTIC)
                .insymd(fromDate.format(YMD))
                .ineymd(toDate.format(YMD))
                .pageNo(String.valueOf(pageNo))
                .dmcnt(String.valueOf(PAGE_SIZE))
                .build();
    }

    private boolean hasNext(NhCardApprovalResponse res) {
        return res != null && "Y".equalsIgnoreCase(res.getCtntDataYn());
    }
}
