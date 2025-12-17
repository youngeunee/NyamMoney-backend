package com.ssafy.project.common.util;

import java.time.LocalDateTime;

public class CursorUtil {

    public record Cursor(LocalDateTime createdAt, Long postId) {}

    public static Cursor parse(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;

        String[] parts = cursor.split("\\|");
        return new Cursor(
                LocalDateTime.parse(parts[0]),
                Long.parseLong(parts[1])
        );
    }

    public static String format(LocalDateTime createdAt, Long postId) {
        return createdAt.toString() + "|" + postId;
    }
}
