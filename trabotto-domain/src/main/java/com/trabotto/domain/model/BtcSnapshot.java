package com.trabotto.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Snapshot of BTC behavior used as contextual reference during monitoring.
 */
public record BtcSnapshot(
    Instant timestamp,
    BigDecimal price,
    BigDecimal changePercent24h,
    String trend
) {
}
