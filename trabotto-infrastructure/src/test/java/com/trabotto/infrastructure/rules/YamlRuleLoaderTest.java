package com.trabotto.infrastructure.rules;

import com.trabotto.domain.model.Rule;
import com.trabotto.domain.model.RuleSet;
import com.trabotto.domain.model.TradeAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlRuleLoaderTest {

    private static RuleSet ruleSet;

    @BeforeAll
    static void loadRules() throws IOException {
        Path yamlPath = Path.of("src/main/resources/consolidated-rules.yml").toAbsolutePath();
        // Follow symlink to actual file
        if (!yamlPath.toFile().exists()) {
            yamlPath = Path.of("../knowledge/rules/consolidated-rules.yml").toAbsolutePath();
        }
        YamlRuleLoader loader = new YamlRuleLoader(new FileSystemResource(yamlPath));
        ruleSet = loader.consolidatedRuleSet();
    }

    @Test
    void loadsMoreThan700Rules() {
        assertTrue(ruleSet.rules().size() > 700,
                "Expected > 700 rules, got " + ruleSet.rules().size());
    }

    @Test
    void ruleSetMetadataPopulated() {
        assertEquals("consolidated", ruleSet.id());
        assertEquals("1.0", ruleSet.version());
        assertNotNull(ruleSet.createdAt());
    }

    @Test
    void knownRuleIdExists() {
        boolean found = ruleSet.rules().stream()
                .anyMatch(r -> "BULL_T_14_06".equals(r.id()));
        assertTrue(found, "Expected rule BULL_T_14_06 to exist");
    }

    @Test
    void rulesHaveTagsInDescription() {
        Rule first = ruleSet.rules().get(0);
        assertNotNull(first.description(), "Rule description (tags) should not be null");
        assertFalse(first.description().isBlank(), "Rule description (tags) should not be blank");
    }

    @Test
    void actionMappingWorks() {
        assertEquals(TradeAction.ENTER_LONG, YamlRuleLoader.mapAction(List.of("ENTER_LONG")));
        assertEquals(TradeAction.ENTER_SHORT, YamlRuleLoader.mapAction(List.of("ENTER_SHORT")));
        assertEquals(TradeAction.SKIP, YamlRuleLoader.mapAction(List.of("SKIP_TRADE")));
        assertEquals(TradeAction.SKIP, YamlRuleLoader.mapAction(List.of("EXIT_POSITION")));
        assertEquals(TradeAction.AMBIGUOUS, YamlRuleLoader.mapAction(List.of("WAIT")));
        assertEquals(TradeAction.AMBIGUOUS, YamlRuleLoader.mapAction(List.of("MONITOR_LEVEL")));
        assertEquals(TradeAction.SKIP, YamlRuleLoader.mapAction(List.of()));
    }
}
