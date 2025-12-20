package com.ssafy.project.api.v1.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantItem {
    private String bizesId;     // 상가업소번호
    private String bizesNm;     // 상호명
    private String indsLclsNm;  // 업종 대분류명
    private String indsMclsNm;  // 업종 중분류명
    private String indsSclsNm;  // 업종 소분류명
    private Double lon;         // 경도
    private Double lat;         // 위도

}
