package com.ssafy.project.api.v1.api.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseBody {
    private List<MerchantItem> items;
    private int numOfRows;
    private int pageNo;
    private int totalCount;
}
