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
}
