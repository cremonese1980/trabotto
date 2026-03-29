package com.trabotto.domain.model;

import java.util.List;

/**
 * Output produced by deterministic rule evaluation for a signal.
 */
public record RuleVerdict(
    String ruleSetId,
    String ruleSetVersion,
    TradeAction action,
    int confidence,
    List<String> matchedRuleIds,
    String rationale
) {
    public RuleVerdict {
        ruleSetId = requireNonBlank(ruleSetId, "ruleSetId");
        ruleSetVersion = requireNonBlank(ruleSetVersion, "ruleSetVersion");
        action = requireNonNull(action, "action");
        validateConfidence(confidence);
        matchedRuleIds = List.copyOf(requireNonNull(matchedRuleIds, "matchedRuleIds"));
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
