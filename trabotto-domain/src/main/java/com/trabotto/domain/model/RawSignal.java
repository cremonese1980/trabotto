package com.trabotto.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * Raw signal payload as produced by an ingestion adapter before parsing.
 */
public record RawSignal(
    String sourceId,
    Instant receivedAt,
    String rawContent,
    Map<String, String> metadata
) {
}
