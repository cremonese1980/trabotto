package com.trabotto.domain.model;

import java.util.List;

/**
 * Result of pre-execution policy checks before order approval.
 */
public record PolicyCheckResult(
    boolean approved,
    List<String> violations,
    String summary
) {
}
