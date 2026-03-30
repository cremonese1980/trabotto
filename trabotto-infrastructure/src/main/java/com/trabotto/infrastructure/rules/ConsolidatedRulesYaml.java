package com.trabotto.infrastructure.rules;

import java.util.List;
import java.util.Map;

/**
 * Top-level DTO for deserializing consolidated-rules.yml.
 */
public record ConsolidatedRulesYaml(
    Map<String, Object> metadata,
    List<ConsolidatedRule> rules
) {
}
