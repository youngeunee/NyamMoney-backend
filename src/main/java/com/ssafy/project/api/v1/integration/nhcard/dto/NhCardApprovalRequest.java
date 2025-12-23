package com.ssafy.project.api.v1.integration.nhcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NH 개인(법인)카드 승인내역 조회 - Request DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhCardApprovalRequest {
	
    @JsonProperty("Header")
    private NhCardRequestHeader header;
    
    @JsonProperty("FinCard")
    private String finCard;   // 30, 필수

    @JsonProperty("IousDsnc")
    private String iousDsnc;  // 1, 필수 (1:국내 2:해외)

    @JsonProperty("Insymd")
    private String insymd;    // 8, 필수 (yyyyMMdd)

    @JsonProperty("Ineymd")
    private String ineymd;    // 8, (yyyyMMdd)

    @JsonProperty("PageNo")
    private String pageNo;    // 4, default 1

    @JsonProperty("Dmcnt")
    private String dmcnt;     // 10, 필수 (최대 15)
}
