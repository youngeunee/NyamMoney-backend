package com.ssafy.project.api.v1.brand.service;

import org.springframework.stereotype.Service;

import com.ssafy.project.api.v1.brand.mapper.BrandMapper;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandMapper brandMapper;

    public BrandServiceImpl(BrandMapper brandMapper) {
        this.brandMapper = brandMapper;
    }

    @Override
    public Long findBrand(String merchantName) {
        if (merchantName == null) return null;

        String normalized = merchantName.replaceAll("\\s+", "");
        if (normalized.isBlank()) return null;

        return brandMapper.findBrand(normalized);
    }
}
