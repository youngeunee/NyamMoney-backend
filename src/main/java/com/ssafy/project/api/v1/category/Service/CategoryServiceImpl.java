package com.ssafy.project.api.v1.category.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssafy.project.api.v1.category.dto.CategoryItem;
import com.ssafy.project.api.v1.category.dto.CategoryListResponse;
import com.ssafy.project.api.v1.category.mapper.CategoryMapper;

@Service
public class CategoryServiceImpl implements CategoryService{
    
	private final CategoryMapper categoryMapper;
	
	public CategoryServiceImpl(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	@Override
	@Transactional(readOnly = true)
	public CategoryListResponse getCategories() {
		
		List<CategoryItem> list = categoryMapper.selectAll();
		
        return new CategoryListResponse(list.size(), list);
	}

}
