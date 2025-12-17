package com.ssafy.project.common.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class CursorPage<T> {

    private final List<T> items;      // 이번 응답 데이터
    private final String nextCursor;  // 다음 요청에 사용할 커서 (없으면 null)
    private final boolean hasNext;    // 다음 페이지 존재 여부
    
    private final long totalCount;
}
