package com.ssafy.project.api.v1.category.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.api.v1.category.dto.CategoryListResponse;
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;

@Service
public class CategoryServiceImpl implements CategoryService{
    
	private final CategoryMapper categoryMapper;
    private final ObjectProvider<VectorStore> vectorStoreProvider;

	public CategoryServiceImpl(CategoryMapper categoryMapper, ObjectProvider<VectorStore> vectorStoreProvider) {
		this.categoryMapper = categoryMapper;
		this.vectorStoreProvider = vectorStoreProvider;
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryListResponse getCategories() {
		
		List<CategoryItem> list = categoryMapper.selectAll();
		
        return new CategoryListResponse(list.size(), list);
	}
	

	@Override
	public String mapCategoryByIndustry(String industryLcls, String industryMcls) {
		if (industryLcls == null) return "기타";
		
		switch (industryLcls) {
        case "소매":
            return mapRetailCategory(industryMcls);  // 소매 대분류 -> 중분류에 따라 카테고리 매핑
        
        case "도매":
        	return mapWholesaleCategory(industryMcls);
        
        case "음식":
        	return mapFoodCategory(industryMcls);
        	
        case "농업·임업·어업":
            return "식비";
        
        case "제조업":
        	return mapManufacturingCategory(industryMcls);
        
        case "교육":
        	return "교육";
        
        case "금융·보험":
        	return "금융";

        case "보건의료":
        	return "건강·의료";
        
        case "부동산":
        case "전기·가스·증기 공급":
        case "수도·하수 처리·원료 재생업":
        case "공공 행정·국방":
        	return "주거·생활요금";
        
        case "예술·스포츠":
        case "정보통신":
        case "숙박":
        case "시설관리·임대":
        	return "여가·취미";
        	

        default:
            return "기타";
		}
	}
	
	private String mapFoodCategory(String industryMcls) {
		if (industryMcls == null) return "식비";
		
		if (industryMcls.equals("비알코올")) return "카페·간식";

		return "식비";
	}

	private String mapWholesaleCategory(String industryMcls) {
		if (industryMcls == null) return "쇼핑";
		
		switch (industryMcls) {
		case "건축자재·난방 도매":
			return "주거·생활요금";
		
		case "농축산 도매":
		case "음식료·담배 도매":
			return "식비";
			
		default:
			return "쇼핑";
		}
	}

	// 소매 대분류에 따른 카테고리 매핑
	@Override
	public String mapRetailCategory(String industryMcls) {
        if (industryMcls == null) return "쇼핑";  // 중분류가 없으면 기본값 "쇼핑"

        switch (industryMcls) {
        	case "음료 소매":
        		return "카페·간식";
        		
            case "기타 생활용품 소매":
            	return "주거·생활요금";
            			
            case "식료품 소매":
            	return "식비";
            	
            case "자동차 부품 소매":
            case "자동차소매":
            	return "이동·차량";
            	
            case "가구 소매":
            case "가전·통신 소매":
            	return "주거·생활요금";
            	
            case "의약·화장품 소매":
            case "안경·정밀기기 소매":
            	return "건강·의료";
            	
            case "오락용품 소매":
                return "여가·취미";  // 오락용품 소매는 "여가·취미"
            default:
                return "쇼핑";  // 기본적으로 "쇼핑"
        }
    }

	@Override
	public String mapManufacturingCategory(String industryMcls) {
        if (industryMcls == null) return "기타";

        switch (industryMcls) {
            case "가구":
            case "가정용기기":
            	return "주거·생활요금";
            	
                
            case "기초화학":
            case "기초의약":
            case "의료용 기기":
            case "의료용품":
            case "의약품":
                return "건강·의료";
            
            case "비알코올 음료":
            	return "카페·간식";
            	
            case "기타 식품":
            case "곡물·전분":
            case "낙농 빙과":
            case "과실·채소":
            case "육류":
                return "식비";
               
            case "자동차 구부품":
            case "자동차 신부품":
            case "자동차·엔진":
            case "자동차차체":
           
            default:
                return "쇼핑";
        }
    }

	@Override
	public Long findVector(String merchantName) {

	    if (merchantName == null || merchantName.isBlank()) {
	        return null;
	    }

	    VectorStore vectorStore = vectorStoreProvider.getObject();

	    // 1) 벡터 검색 (TOP_K = 5)
	    SearchRequest request = SearchRequest.builder()
	            .query(merchantName)
	            .topK(5)
	            // 구현체에 따라 threshold 의미가 다를 수 있어서(확실하지 않음)
	            // 일단 넣되, 아래에서 score로 한 번 더 필터링해서 안전하게 갑니다.
	            .similarityThreshold(0.20)
	            .build();

	    List<Document> docs = vectorStore.similaritySearch(request);
	    if (docs == null || docs.isEmpty()) {
	        return null;
	    }

	    // 2) (안전장치) score <= 0.20 필터링 (distance 기준: 작을수록 유사)
	    List<Document> filtered = docs.stream()
	            .filter(d -> {
	                Object scoreObj = d.getMetadata() == null ? null : d.getMetadata().get("score");
	                if (scoreObj == null) return false;
	                try {
	                    double score = Double.parseDouble(scoreObj.toString());
	                    return score <= 0.20;
	                } catch (NumberFormatException e) {
	                    return false;
	                }
	            })
	            .toList();

	    if (filtered.isEmpty()) {
	        return null;
	    }

	    // 3) categoryId 기준 다수결
	    Map<Long, List<Document>> grouped = filtered.stream()
	            .filter(d -> d.getMetadata() != null && d.getMetadata().get("category") != null)
	            .collect(Collectors.groupingBy(d -> {
	                try {
	                    return Long.parseLong(d.getMetadata().get("category").toString());
	                } catch (NumberFormatException e) {
	                    return -1L; // 잘못된 카테고리 값은 버리기 위한 임시 값
	                }
	            }));

	    grouped.remove(-1L);

	    // 4) 2개 이상인 category만 후보, 동률이면 평균 score가 더 작은 쪽(=더 유사) 우선
	    return grouped.entrySet().stream()
	            .filter(e -> e.getValue().size() >= 2)
	            .max(Comparator
	                    .comparingInt((Map.Entry<Long, List<Document>> e) -> e.getValue().size())
	                    .thenComparingDouble(e ->
	                            -e.getValue().stream()
	                                    .mapToDouble(d -> Double.parseDouble(d.getMetadata().get("score").toString()))
	                                    .average()
	                                    .orElse(Double.POSITIVE_INFINITY)
	                    )
	            )
	            .map(Map.Entry::getKey)
	            .orElse(null);
	}


}
