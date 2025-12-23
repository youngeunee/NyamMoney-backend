package com.ssafy.project.api.v1.category.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.category.dto.CategoryListResponse;
import com.ssafy.project.api.v1.category.service.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
	private final CategoryService categoryService;
	
	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}
	
	@GetMapping
	public ResponseEntity<CategoryListResponse> getCategories() {
		CategoryListResponse res = categoryService.getCategories();
		return ResponseEntity.ok(res);
	}
}
