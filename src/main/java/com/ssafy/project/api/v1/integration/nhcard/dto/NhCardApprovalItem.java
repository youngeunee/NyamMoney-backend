package com.ssafy.project.api.v1.integration.nhcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
* NH 승인내역 목록의 1건(REC item) DTO
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhCardApprovalItem {

 @JsonProperty("CardAthzNo")
 private String cardAthzNo; // 20

 @JsonProperty("Trdd")
 private String trdd;       // 8 yyyyMMdd

 @JsonProperty("Txtm")
 private String txtm;       // 6 HHmmss

 @JsonProperty("Usam")
 private String usam;       // 19 금액(문자열)

 @JsonProperty("AfstNoBrno")
 private String afstNoBrno; // 10

 @JsonProperty("AfstNo")
 private String afstNo;     // 20

 @JsonProperty("AfstNm")
 private String afstNm;     // 50

 @JsonProperty("AmslKnd")
 private String amslKnd;    // 1 매출종류(1/2/3/6/7/8)

 @JsonProperty("Tris")
 private String tris;       // 2 할부기간(00 등)

 @JsonProperty("Ccyn")
 private String ccyn;       // 1 정상:0, 취소:1

 @JsonProperty("CnclYmd")
 private String cnclYmd;    // 8 취소일자(yyyyMMdd) or ""

 @JsonProperty("Crcd")
 private String crcd;       // 3 통화코드 or ""

 @JsonProperty("AcplUsam")
 private String acplUsam;   // 19 현지이용금액 or ""
}

