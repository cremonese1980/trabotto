package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Standard OHLCV market candle.
 */
public record Candle(
    Instant openTime,
    Instant closeTime,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    BigDecimal volume
) {
}
