package com.ssafy.project.api.v1.api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.project.api.v1.api.dto.ApiResponse;
import com.ssafy.project.api.v1.api.dto.MerchantItem;
import com.ssafy.project.api.v1.category.Service.CategoryService;
import com.ssafy.project.redis.vector.RedisVectorStore;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ApiDataServiceImpl implements ApiDataService {
	private final CategoryService categoryService;
    private final RedisVectorStore redisVectorStore;
    private final VectorizeUtil vectorizeUtil;
    private final RestTemplate restTemplate;
    
	@Override
	public void fetchDataAndStore() {
	    String apiKey = "Gljm+YiEpuxaXzPW6URJvDrMneV9HHYoaQ8IJDh7aiLFz2rkwighYvH7TEAjH5+3lPrg7I6/OGodRJ/8CWL8Bw==";  // 여기에 자신의 API 키 입력
	    int startSggCode = 10000;  // 시작 상권 번호
	    int endSggCode = 10508;    // 끝 상권 번호

	    for (int sggCode = startSggCode; sggCode <= endSggCode; sggCode++) {
            int pageNo = 1;
            int totalPages = 1;  // 먼저 첫 페이지만 받기 위해 초기화

            // 상권번호를 사용하여 데이터를 호출합니다.
            while (pageNo <= totalPages) {
                String url = String.format(
                    "https://apis.data.go.kr/B553077/api/open/sdsc2/storeListInArea?ServiceKey=%s&pageNo=%d&numOfRows=100&key=%d&type=json", 
                    apiKey, pageNo, sggCode
                );

                try {
                    // API 호출하여 JSON 응답을 받아옵니다
                    String response = restTemplate.getForObject(url, String.class);

                    // JSON 응답을 파싱하여 ApiResponse 객체로 변환
                    ApiResponse apiResponse = parseApiResponse(response);  // JSON 파싱 메서드

                    // 파싱된 아이템 목록 처리
                    for (MerchantItem item : apiResponse.getBody().getItems()) {
                        // 카테고리 매핑
                        String category = categoryService.mapCategoryByIndustry(
                            item.getIndsLclsNm(), item.getIndsMclsNm()
                        );

                        // 상호명 벡터화
                        String vector = vectorizeUtil.vectorize(item.getBizesNm());

                        // Redis에 저장
                        redisVectorStore.put(item.getBizesNm(), vector, category);
                    }

                    // 페이지 정보를 받아서 다음 페이지를 처리하도록 합니다.
                    totalPages = apiResponse.getBody().getTotalCount();  // 예시로 totalPages를 받아옴
                    pageNo++;
                    
                } catch (Exception e) {
                    // 예외 처리
                    e.printStackTrace();
                }
            }
        }
    }

    // JSON 응답을 ApiResponse 객체로 파싱하는 메서드
    private ApiResponse parseApiResponse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(response, ApiResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}