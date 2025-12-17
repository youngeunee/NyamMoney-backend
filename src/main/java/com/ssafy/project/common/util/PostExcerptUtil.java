package com.ssafy.project.common.util;

public class PostExcerptUtil {

    private static final int MAX_LENGTH = 100;

    public static String makeExcerpt(String content) {
        if (content == null || content.isBlank()) return "";

        // 줄바꿈 → 공백
        String normalized = content
                .replace("\r\n", " ")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= MAX_LENGTH) {
            return normalized;
        }

        return normalized.substring(0, MAX_LENGTH) + "…";
    }
}
