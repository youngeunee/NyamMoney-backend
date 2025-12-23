package com.ssafy.project.api.v1.category.service;

import com.ssafy.project.api.v1.category.dto.CategoryListResponse;

public interface CategoryService {

	CategoryListResponse getCategories();
	
	public String mapCategoryByIndustry(String industryLcls, String industryMcls);
	
	public String mapRetailCategory(String industryMcls);
	
	public String mapManufacturingCategory(String industryMcls);

	Long findVector(String merchantName);

}
