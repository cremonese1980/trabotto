package com.trabotto.infrastructure.rules;

import com.trabotto.domain.model.Rule;
import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.TradeAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads consolidated-rules.yml at startup, deserializes into ConsolidatedRule DTOs,
 * and converts them to a domain RuleSet exposed as a Spring bean.
 */
@Component
public class YamlRuleLoader {

    private static final Logger log = LoggerFactory.getLogger(YamlRuleLoader.class);

    private final Resource rulesResource;

    public YamlRuleLoader(@Value("${trabotto.rules.path:classpath:consolidated-rules.yml}") Resource rulesResource) {
        this.rulesResource = rulesResource;
    }

    @Bean
    public RuleSet consolidatedRuleSet() throws IOException {
        List<ConsolidatedRule> consolidated = loadConsolidatedRules();
        log.info("Loaded {} rules from consolidated-rules.yml", consolidated.size());

        List<Rule> domainRules = consolidated.stream()
                .map(YamlRuleLoader::toDomainRule)
                .toList();

        return new RuleSet(
                "consolidated",
                "1.0",
                "consolidated-rules.yml",
                Instant.now(),
                domainRules
        );
    }

    @SuppressWarnings("unchecked")
    List<ConsolidatedRule> loadConsolidatedRules() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream is = rulesResource.getInputStream()) {
            Map<String, Object> root = yaml.load(is);
            List<Map<String, Object>> rawRules = (List<Map<String, Object>>) root.get("rules");
            if (rawRules == null) {
                return List.of();
            }

            List<ConsolidatedRule> result = new ArrayList<>(rawRules.size());
            for (Map<String, Object> raw : rawRules) {
                result.add(mapToConsolidatedRule(raw));
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private static ConsolidatedRule mapToConsolidatedRule(Map<String, Object> raw) {
        return new ConsolidatedRule(
                str(raw, "id"),
                str(raw, "entity_type"),
                str(raw, "category"),
                str(raw, "scope"),
                str(raw, "severity"),
                strList(raw, "tags"),
                (Map<String, String>) raw.get("source"),
                strList(raw, "conditions"),
                strList(raw, "action"),
                str(raw, "stop_loss"),
                str(raw, "take_profit"),
                (Map<String, List<String>>) raw.get("applicability"),
                str(raw, "rationale"),
                str(raw, "notes"),
                strList(raw, "source_files")
        );
    }

    private static String str(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : v.toString();
    }

    @SuppressWarnings("unchecked")
    private static List<String> strList(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    static Rule toDomainRule(ConsolidatedRule cr) {
        String tagsJoined = String.join(",", cr.tags() != null ? cr.tags() : List.of());
        String conditionsJoined = String.join(" AND ", cr.conditions() != null ? cr.conditions() : List.of());
        TradeAction action = mapAction(cr.action());
        int priority = "HARD".equals(cr.severity()) ? 1 : 2;

        return new Rule(
                cr.id(),
                cr.entity_type(),
                tagsJoined,
                priority,
                true,
                action,
                conditionsJoined
        );
    }

    static TradeAction mapAction(List<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return TradeAction.SKIP;
        }
        for (String a : actions) {
            return switch (a) {
                case "ENTER_LONG" -> TradeAction.ENTER_LONG;
                case "ENTER_SHORT" -> TradeAction.ENTER_SHORT;
                case "SKIP_TRADE", "EXIT_POSITION" -> TradeAction.SKIP;
                default -> TradeAction.AMBIGUOUS;
            };
        }
        return TradeAction.SKIP;
    }
}
