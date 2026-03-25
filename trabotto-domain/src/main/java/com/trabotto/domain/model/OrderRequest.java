package com.trabotto.domain.model;

import java.math.BigDecimal;

/**
 * Immutable order request sent to an exchange adapter.
 */
public record OrderRequest(
    String signalId,
    String pair,
    TradeAction action,
    BigDecimal quantity,
    BigDecimal entryPrice,
    BigDecimal stopLoss,
    BigDecimal takeProfit,
    String timeframe,
    String strategy
) {
}
