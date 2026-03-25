package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * Counterfactual result describing what a shadow trade would have produced.
 */
public record ShadowTradeOutcome(
    String shadowTradeId,
    boolean wouldHitTakeProfit,
    boolean wouldHitStopLoss,
    BigDecimal bestPnl,
    BigDecimal worstPnl,
    Duration timeToOutcome,
    String notes
) {
}
