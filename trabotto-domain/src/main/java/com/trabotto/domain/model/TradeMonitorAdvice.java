package com.trabotto.domain.model;

/**
 * AI recommendation for dynamic management of an open position.
 */
public record TradeMonitorAdvice(
    MonitorAction action,
    String rationale,
    int confidence,
    boolean valid
) {
    public TradeMonitorAdvice {
        action = requireNonNull(action, "action");
        rationale = requireNonBlank(rationale, "rationale");
        validateConfidence(confidence);
    }

    private static void validateConfidence(int confidence) {
        if (confidence < 0 || confidence > 100) {
            throw new IllegalArgumentException("confidence must be between 0 and 100");
        }
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
