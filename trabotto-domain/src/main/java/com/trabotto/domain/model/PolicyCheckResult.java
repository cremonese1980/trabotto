package com.trabotto.domain.model;

import java.util.List;

/**
 * Result of pre-execution policy checks before order approval.
 */
public record PolicyCheckResult(
    boolean approved,
    List<String> violations,
    String summary
) {
    public PolicyCheckResult {
        violations = List.copyOf(requireNonNull(violations, "violations"));
        summary = requireNonBlank(summary, "summary");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
