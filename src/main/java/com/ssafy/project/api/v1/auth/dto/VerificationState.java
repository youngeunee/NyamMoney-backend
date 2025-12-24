package com.ssafy.project.api.v1.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationState {
    private final int attemptCount;
    private final long resendBlockedUntil;

    public String toValue() {
        return attemptCount + "|" + resendBlockedUntil;
    }

    public static VerificationState from(String value) {
        if (value == null || value.isBlank()) {
            return new VerificationState(0, 0L);
        }
        String[] parts = value.split("\\|");
        return new VerificationState(
                Integer.parseInt(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}