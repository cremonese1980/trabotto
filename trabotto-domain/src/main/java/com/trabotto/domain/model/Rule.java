package com.trabotto.domain.model;

/**
 * Single deterministic rule used by the rule engine.
 */
public record Rule(
    String id,
    String name,
    String description,
    int priority,
    boolean enabled,
    TradeAction action,
    String condition
) {
}
