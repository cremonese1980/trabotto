package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Final immutable outcome for a position after closure.
 */
public record TradeOutcome(
    String positionId,
    String pair,
    TradeAction side,
    BigDecimal entryPrice,
    BigDecimal exitPrice,
    BigDecimal pnl,
    BigDecimal fees,
    Duration duration,
    Instant closedAt,
    String closeReason
) {
}
