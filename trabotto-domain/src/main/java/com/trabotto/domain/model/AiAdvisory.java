package com.trabotto.domain.model;

/**
 * AI recommendation for a potential entry, always treated as advisory input.
 */
public record AiAdvisory(
    TradeAction action,
    int confidence,
    String rationale,
    boolean valid
) {
    public AiAdvisory {
        action = requireNonNull(action, "action");
        validateConfidence(confidence);
        rationale = requireNonBlank(rationale, "rationale");
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
