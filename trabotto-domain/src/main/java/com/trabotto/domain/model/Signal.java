package com.trabotto.domain.model;

import java.time.Instant;

/**
 * Normalized trading signal emitted by the signal normalization layer.
 */
public record Signal(
    String id,
    Instant timestamp,
    String pair,
    String timeframe,
    String strategy,
    String exchange,
    boolean perfezione,
    String sourceId,
    String rawMessage
) {
}
