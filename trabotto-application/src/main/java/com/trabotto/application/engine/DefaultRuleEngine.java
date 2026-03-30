package com.trabotto.application.engine;

import com.trabotto.domain.engine.RuleEngine;
import com.trabotto.domain.model.Rule;
import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.RuleVerdict;
import com.trabotto.domain.model.Signal;
import com.trabotto.domain.model.TradeAction;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Rule engine v1: matches rules against signals by strategy tag.
 *
 * Matching logic:
 * 1. Filter rules where description (comma-separated tags) contains the signal strategy
 * 2. Filter rules where name (entity_type) is SETUP or RULE (skip INVALIDATION for entry)
 * 3. Calculate confidence: base 50 + 10 per matching rule + 20 if perfezione, cap 100
 * 4. Pick most common action among matched rules
 */
@Service
public class DefaultRuleEngine implements RuleEngine {

    @Override
    public RuleVerdict evaluate(Signal signal, RuleSet ruleSet) {
        String strategy = signal.strategy();
        if (strategy == null || strategy.isBlank()) {
            return noMatch(ruleSet, "No strategy in signal");
        }

        String strategyLower = strategy.toLowerCase();

        // Step 1+2: filter by tag match AND entity_type (SETUP or RULE only)
        List<Rule> matched = ruleSet.rules().stream()
                .filter(r -> containsTag(r.description(), strategyLower))
                .filter(r -> "SETUP".equals(r.name()) || "RULE".equals(r.name()))
                .toList();

        if (matched.isEmpty()) {
            return noMatch(ruleSet, "No rules matched strategy '" + strategy + "'");
        }

        // Step 3: confidence
        int confidence = 50 + (10 * matched.size()) + (signal.perfezione() ? 20 : 0);
        confidence = Math.min(confidence, 100);

        // Step 4: most common action
        TradeAction action = mostCommonAction(matched);

        // Step 5: build verdict
        List<String> matchedIds = matched.stream().map(Rule::id).toList();
        String rationale = "Matched %d rules for strategy '%s': %s".formatted(
                matched.size(), strategy, String.join(", ", matchedIds));

        return new RuleVerdict(
                ruleSet.id(),
                ruleSet.version(),
                action,
                confidence,
                matchedIds,
                rationale
        );
    }

    private static boolean containsTag(String tagsCommaJoined, String strategy) {
        if (tagsCommaJoined == null || tagsCommaJoined.isBlank()) {
            return false;
        }
        return Arrays.stream(tagsCommaJoined.split(","))
                .map(String::trim)
                .anyMatch(tag -> tag.equalsIgnoreCase(strategy));
    }

    private static TradeAction mostCommonAction(List<Rule> rules) {
        Map<TradeAction, Long> counts = rules.stream()
                .collect(Collectors.groupingBy(Rule::action, Collectors.counting()));

        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(TradeAction.SKIP);
    }

    private static RuleVerdict noMatch(RuleSet ruleSet, String reason) {
        return new RuleVerdict(
                ruleSet.id(),
                ruleSet.version(),
                TradeAction.SKIP,
                0,
                List.of(),
                reason
        );
    }
}
