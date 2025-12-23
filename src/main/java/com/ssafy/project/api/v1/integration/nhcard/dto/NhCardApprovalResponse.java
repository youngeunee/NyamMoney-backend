package com.ssafy.project.api.v1.integration.nhcard.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
* NH 개인(법인)카드 승인내역 조회 - Response DTO
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhCardApprovalResponse {

 @JsonProperty("Header")
 private NhCardResponseHeader header;

 @JsonProperty("PageNo")
 private String pageNo;

 @JsonProperty("Dmcnt")
 private String dmcnt;

 @JsonProperty("CtntDataYn")
 private String ctntDataYn; // Y: 이후 데이터 있음, N: 없음

 @JsonProperty("TotCnt")
 private String totCnt;

 @JsonProperty("Iqtcnt")
 private String iqtcnt;

 @JsonProperty("REC")
 private List<NhCardApprovalItem> items;
}
