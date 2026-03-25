package com.trabotto.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Versioned collection of rules associated with a source or strategy.
 */
public record RuleSet(
    String id,
    String version,
    String sourceId,
    Instant createdAt,
    List<Rule> rules
) {
}
