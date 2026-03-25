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
}
