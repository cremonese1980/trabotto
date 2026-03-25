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
}
