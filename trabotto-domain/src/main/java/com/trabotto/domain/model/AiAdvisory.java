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
}
