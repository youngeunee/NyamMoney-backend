package com.ssafy.project.api.v1.integration.nhcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NH 개인(법인)카드 승인내역 조회 - Request Header DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhCardRequestHeader {

    @JsonProperty("ApiNm")
    private String apiNm;            // InquireCreditCardAuthorizationHistory

    @JsonProperty("Tsymd")
    private String tsymd;            // 요청일자 yyyyMMdd

    @JsonProperty("Trtm")
    private String trtm;             // 요청시간 HHmmss

    @JsonProperty("Iscd")
    private String iscd;             // 기관코드 (고정)

    @JsonProperty("FintechApsno")
    private String fintechApsno;     // 핀테크 앱 일련번호 (보통 "001")

    @JsonProperty("ApiSvcCd")
    private String apiSvcCd;          // CardInfo

    @JsonProperty("IsTuno")
    private String isTuno;            // 거래고유번호 (요청마다 유니크)

    @JsonProperty("AccessToken")
    private String accessToken;       // 접근토큰
}
