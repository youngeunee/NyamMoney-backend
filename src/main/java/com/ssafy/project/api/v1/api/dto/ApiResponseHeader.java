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
public class ApiResponseHeader {
    private String description;
    private List<String> columns;
    private String resultCode;
    private String resultMsg;
}
