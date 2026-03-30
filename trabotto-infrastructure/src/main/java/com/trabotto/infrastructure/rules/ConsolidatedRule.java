package com.trabotto.infrastructure.rules;

import java.util.List;
import java.util.Map;

/**
 * DTO for deserializing a single rule entry from consolidated-rules.yml.
 * Converted to domain Rule by YamlRuleLoader after loading.
 */
public record ConsolidatedRule(
    String id,
    String entity_type,
    String category,
    String scope,
    String severity,
    List<String> tags,
    Map<String, String> source,
    List<String> conditions,
    List<String> action,
    String stop_loss,
    String take_profit,
    Map<String, List<String>> applicability,
    String rationale,
    String notes,
    List<String> source_files
) {
}
