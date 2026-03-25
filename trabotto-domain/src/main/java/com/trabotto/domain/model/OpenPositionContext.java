package com.trabotto.domain.model;

import java.util.List;

/**
 * Monitoring context for a currently open position.
 */
public record OpenPositionContext(
    Position position,
    List<Candle> candlesSinceEntry,
    double unrealizedPnlPercent,
    int elapsedMinutes
) {
}
