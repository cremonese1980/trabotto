package com.trabotto.domain.model;

/**
 * Deterministic constraints applied to AI outputs before they can influence decisions.
 */
public record AiConstraints(
    int minConfidence,
    boolean allowEntryDecision,
    boolean allowExitDecision,
    String policyProfile
) {
}
