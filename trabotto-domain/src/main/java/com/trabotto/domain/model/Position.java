package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Open trading position managed by the monitoring pipeline.
 */
public record Position(
    String id,
    String pair,
    TradeAction side,
    BigDecimal quantity,
    BigDecimal entryPrice,
    BigDecimal stopLoss,
    BigDecimal takeProfit,
    Instant openedAt,
    String exchange,
    String timeframe,
    String strategy,
    String signalId
) {
}
