package com.ssafy.project.api.v1.integration.nhcard.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.integration.nhcard.caller.NhCardCaller;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalRequest;
import com.ssafy.project.api.v1.integration.nhcard.dto.NhCardApprovalResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/test/nhcard")
@RequiredArgsConstructor
@Slf4j
public class NhCardTestController {

    private final NhCardCaller nhCardCaller;

    @PostMapping("/call")
    public String testCall() {
        NhCardApprovalRequest req = NhCardApprovalRequest.builder()
                .finCard("00829101234560000112345678919")
                .iousDsnc("1")
                .insymd("20191105")
                .ineymd("20191109")
                .pageNo("1")
                .dmcnt("15")
                .build();

        NhCardApprovalResponse res =
                nhCardCaller.inquireApprovals(req);
        log.debug("result: ", res.toString());
        return "rpcd=" + res.getHeader().getRpcd()
             + ", count=" + res.getItems().size()
             + ", ctnt=" + res.getCtntDataYn();
    }
}
