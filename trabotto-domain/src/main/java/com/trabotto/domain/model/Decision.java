package com.trabotto.domain.model;

import java.time.Instant;

/**
 * Full decision payload that aggregates deterministic rules, AI input, and policies.
 */
public record Decision(
    String id,
    Instant decidedAt,
    Signal signal,
    RuleVerdict ruleVerdict,
    AiAdvisory aiAdvisory,
    PolicyCheckResult policyCheck,
    TradeAction finalAction,
    String rationale
) {
    public Decision {
        id = requireNonBlank(id, "id");
        decidedAt = requireNonNull(decidedAt, "decidedAt");
        signal = requireNonNull(signal, "signal");
        ruleVerdict = requireNonNull(ruleVerdict, "ruleVerdict");
        aiAdvisory = requireNonNull(aiAdvisory, "aiAdvisory");
        policyCheck = requireNonNull(policyCheck, "policyCheck");
        finalAction = requireNonNull(finalAction, "finalAction");
        rationale = requireNonBlank(rationale, "rationale");
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
