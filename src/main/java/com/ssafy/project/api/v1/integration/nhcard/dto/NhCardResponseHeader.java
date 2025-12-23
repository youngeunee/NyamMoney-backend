package com.ssafy.project.api.v1.integration.nhcard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
* NH 응답 Header DTO
*
* 주의: 실제 응답에서 "Rsms " 키에 공백이 포함되어 들어옴.
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhCardResponseHeader {

 @JsonProperty("ApiNm")
 private String apiNm;

 @JsonProperty("Tsymd")
 private String tsymd;

 @JsonProperty("Trtm")
 private String trtm;

 @JsonProperty("Iscd")
 private String iscd;

 @JsonProperty("FintechApsno")
 private String fintechApsno;

 @JsonProperty("ApiSvcCd")
 private String apiSvcCd;

 @JsonProperty("IsTuno")
 private String isTuno;

 @JsonProperty("Rpcd")
 private String rpcd;

 // ✅ 실제 키가 "Rsms " (공백 포함)
 @JsonProperty("Rsms ")
 private String rsms;
}
