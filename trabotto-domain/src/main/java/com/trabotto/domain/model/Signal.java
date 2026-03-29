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
    public Signal {
        id = requireNonBlank(id, "id");
        timestamp = requireNonNull(timestamp, "timestamp");
        pair = requireNonBlank(pair, "pair");
        timeframe = requireNonBlank(timeframe, "timeframe");
        strategy = requireNonBlank(strategy, "strategy");
        exchange = requireNonBlank(exchange, "exchange");
        sourceId = requireNonBlank(sourceId, "sourceId");
        rawMessage = requireNonBlank(rawMessage, "rawMessage");
    }

    private static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
        return value;
    }
}
