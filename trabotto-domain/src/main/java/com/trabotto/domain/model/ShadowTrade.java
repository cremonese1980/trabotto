package com.trabotto.domain.model;

import java.time.Instant;

/**
 * Trade intentionally skipped in live execution but tracked for offline analysis.
 */
public record ShadowTrade(
    String id,
    Signal signal,
    Decision decision,
    Instant trackedAt,
    String skipReason
) {
}
