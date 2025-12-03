package com.ssafy.project.domain.category.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {
    private Long categoryId;
    private String name;
    private LocalDateTime createdAt;
}
