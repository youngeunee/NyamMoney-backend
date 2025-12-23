package com.ssafy.project.api.v1.category.model;

public enum CategoryType {
    FOOD(1L, "식비"),
    CAFE_SNACK(2L, "카페·간식"),
    SHOPPING(3L, "쇼핑"),
    TRANSPORT(4L, "이동·차량"),
    HOUSING_BILLS(5L, "주거·생활요금"),
    HEALTH(6L, "건강·의료"),
    EDUCATION(7L, "교육"),
    LEISURE(8L, "여가·취미"),
    FINANCE(9L, "금융"),
    OTHER(10L, "기타");

    private final Long id;
    private final String label;

    CategoryType(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static CategoryType fromLabel(String label) {
        if (label == null) return null;
        for (CategoryType t : values()) {
            if (t.label.equals(label)) return t;
        }
        return null;
    }

    public static CategoryType fromId(Long id) {
        if (id == null) return null;
        for (CategoryType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }
}
